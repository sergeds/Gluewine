/**************************************************************************
 *
 * Gluewine Console Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.console.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.history.FileHistory;

import org.gluewine.authentication.AuthenticationAbortedException;
import org.gluewine.console.AnsiCodes;
import org.gluewine.console.ConsoleServer;
import org.gluewine.console.SyntaxException;
import org.gluewine.gxo_client.GxoClient;

/**
 * Starts the console client.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class ConsoleClient implements Runnable, Completer, AnsiCodes
{
    // ===========================================================================
    /**
     * The GXO Client instance to use.
     */
    private GxoClient client = null;

    /**
     * The prompt to use.
     */
    private String prompt = null;

    /**
     * The server connected to.
     */
    private ConsoleServer server = null;

    /**
     * The welcome string.
     */
    private static final String WELCOME = "\u001b[47m\u001b[30m                                          \n"
                                          + "            \u001b[31mGluewine Framework\u001b[30m            \n"
                                          + "                                          \n"
                                          + "             www.gluewine.org             \n"
                                          + "      GNU Lesser General Public v 3.0     \n"
                                          + "                                          \n"
                                          + "              \u001b[32m(c) FKS bvba\u001b[30m                \n"
                                          + "                                          \n\u001b[0m";

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param host The host running the ConsoleServer.
     * @param port The port.
     */
    private ConsoleClient(String host, int port)
    {
        client = new GxoClient(host, port);
        prompt = host + "> ";
    }

    // ===========================================================================
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL")
    public void run()
    {
        try
        {
            ConsoleReader reader = new ConsoleReader();
            reader.clearScreen();
            reader.setBellEnabled(true);
            reader.println(WELCOME);

            String home = System.getProperty("user.home");
            FileHistory his = new FileHistory(new File(home, ".osgi_history"));
            reader.setHistory(his);
            reader.setHistoryEnabled(true);
            reader.addCompleter(this);

            boolean initial = true;
            boolean stopRequested = false;
            boolean outputRouted = false;
            String outputFile = null;
            BufferedWriter writer = null;
            while (!stopRequested)
            {
                try
                {
                    if (server == null && initial)
                    {
                        server = client.getService(ConsoleServer.class, "-1", getIpAddress());
                        if (server.needsAuthentication())
                            authenticate(reader);
                        initial = false;
                    }

                    String line = reader.readLine(prompt);
                    if (line == null) line = "";

                    if (line.startsWith("exit") || line.startsWith("close"))
                        stopRequested = true;

                    else if (line.equals("cls") || line.equals("clear"))
                        reader.println(CLS + HOME);

                    else if (line.startsWith(">"))
                    {
                        outputFile = line.substring(1).trim();
                        if (outputFile.equals("!"))
                        {
                            if (writer != null)
                            {
                                outputRouted = false;
                                writer.close();
                                writer = null;
                            }
                        }
                        else
                        {
                            outputRouted = true;
                            writer = prepareOutputFile(outputFile);
                        }
                    }

                    else if (line.startsWith("logoff"))
                    {
                        server = null;
                        initial = true;
                    }

                    else if (line.trim().length() > 0)
                    {
                        if (server == null)
                        {
                            server = client.getService(ConsoleServer.class, "-1", getIpAddress());
                            if (server.needsAuthentication())
                                authenticate(reader);
                        }

                        try
                        {
                            List<String> cmds = new ArrayList<String>();
                            cmds.add(line);
                            while (!cmds.isEmpty())
                            {
                                line = cmds.remove(0);
                                String output = null;

                                if (line.startsWith("exec")) output = loadExecutionFile(line.substring(4), cmds);
                                else output = server.executeCommand(line);

                                if (outputRouted)
                                {
                                    writer.write(output);
                                    writer.newLine();
                                    writer.flush();
                                }
                                else reader.println(output);
                            }
                        }
                        catch (SyntaxException e)
                        {
                            System.out.println("\u001b[31;1m" + e.getMessage() + "\u001b[0m");
                        }
                        catch (Throwable e)
                        {
                            if (e instanceof ConnectException)
                            {
                                System.out.println("Connection lost!");
                                prompt = "local>";
                                server = null;
                            }
                            else if (e.getMessage() != null && e.getMessage().startsWith("org.gluewine.sessions.SessionExpiredException:"))
                            {
                                System.out.println("Session Expired!");
                                initial = true;
                                server = null;
                            }

                            else
                                e.printStackTrace();
                        }
                    }
                }
                catch (AuthenticationAbortedException e)
                {
                    System.out.println("Authentication Aborted! Closing console.");
                    stopRequested = true;
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    System.out.println("Cannot connect to server!");
                }
            }
            his.flush();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    /**
     * Loads the commands specified in the given file.
     *
     * @param fileName The file name.
     * @param cmds The list to update.
     * @return Text to be output.
     */
    private String loadExecutionFile(String fileName, List<String> cmds)
    {
        fileName = fileName.trim();
        String output = "Loaded " + fileName;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8")))
        {
            while (in.ready())
            {
                String line = in.readLine().trim();
                if (line.length() > 0) cmds.add(line);
            }
        }
        catch (Throwable e)
        {
            output = e.getMessage();
        }

        return output;
    }

    // ===========================================================================
    /**
     * Prepares the output file.
     *
     * @param file The file.
     * @return The printwriter to use.
     * @throws IOException If an error occurs.
     */
    private BufferedWriter prepareOutputFile(String file) throws IOException
    {
        File f = new File(file);
        if (!f.getParentFile().exists())
            if (!f.getParentFile().mkdirs())
                throw new IOException("Could not create dir: " + f.getParentFile().getAbsolutePath());

        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
    }

    // ===========================================================================
    /**
     * Performs authentication.
     * @throws Throwable If the authentication class could not be found or could not
     * be instantiated.
     *
     * @param reader The current reader to use.
     */
    private void authenticate(ConsoleReader reader) throws Throwable
    {
        String clazz = getAuthenticatorClass(reader);

        Class<?> srvdef = getClass().getClassLoader().loadClass(clazz);
        Class<?> cl = getClass().getClassLoader().loadClass(clazz + "TxtClient");
        Object o = cl.newInstance();
        Class<?>[] parameterTypes = new Class<?>[] {srvdef, ConsoleReader.class};
        Method m = cl.getMethod("authenticate", parameterTypes);

        Object srv = client.getService(srvdef, "-1", getIpAddress());
        Object[] params = new Object[] {srv, reader};
        Object id = m.invoke(o, params);
        if (id instanceof String)
            server = client.getService(ConsoleServer.class, (String) id, getIpAddress());

        String msg = server.getWelcomeMessage();
        if (msg != null) System.out.println(msg);

        String p = server.getPrompt();
        if (p != null && p.length() > 0) prompt = p;
    }

    // ===========================================================================
    /**
     * Returns the authenticator class to use. If there's only one available it will
     * be used. If there are more available the user will be prompted to select one.
     *
     * @param reader The reader to use.
     * @return The clazz choosen by the user.
     * @throws Throwable If an error occurs.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL")
    private String getAuthenticatorClass(ConsoleReader reader) throws Throwable
    {
        Map<String, String> auths = server.getAvailableAuthenticators();
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(auths);
        List<String> indexed = new ArrayList<String>(sorted.size());
        indexed.addAll(sorted.keySet());
        sorted.putAll(auths);
        String clazz = null;

        if (indexed.size() == 1) clazz = sorted.get(indexed.get(0));

        while (clazz == null)
        {
            for (int i = 0; i < indexed.size(); i++)
                System.out.println((i + 1) + "\t" + indexed.get(i));

            String l = reader.readLine("Select an authentication method: ");
            try
            {
                int index = Integer.parseInt(l) - 1;
                if (index >= 0 && index < indexed.size())
                    clazz = sorted.get(indexed.get(index));
            }
            catch (NumberFormatException e)
            {
                System.out.println("Enter a value between 1 and " + indexed.size());
            }
        }

        return clazz;
    }

    // ===========================================================================
    /**
     * Gets the ip address.
     *
     * @return The ip address.
     */
    private String getIpAddress()
    {
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                NetworkInterface inf = en.nextElement();
                if (inf.isUp() && !inf.isVirtual() && !inf.isLoopback())
                {
                    StringBuilder b = new StringBuilder();
                    byte[] mac = inf.getHardwareAddress();
                    for (int i = 0; i < mac.length; i++)
                    {
                        b.append(String.format("%1$02X ", mac[i]));
                        if (i < mac.length - 1)
                            b.append("-");
                    }
                    Enumeration<InetAddress> addrs = inf.getInetAddresses();
                    while (addrs.hasMoreElements())
                    {
                        InetAddress addr = addrs.nextElement();
                        return addr.getHostAddress();
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        return null;
    }

    // ===========================================================================
    @Override
    @SuppressWarnings({"rawtypes", "unchecked" })
    public int complete(String s, int i, List l)
    {
        if (s == null) s = "";
        List<String> r = server.complete(s);
        l.clear();
        l.addAll(r);

        int cursor = 0;
        int b = s.lastIndexOf(' ');
        if (b > -1)
            cursor = b + 1;

        int sep = s.lastIndexOf(File.separator);
        if (sep > -1)
            cursor = sep + 1;

        return cursor;
    }

    // ===========================================================================
    /**
     * Main invocation routine.
     *
     * @param args The CLI arguments.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL")
    public static void main(String[] args)
    {
        if (args == null || args.length < 2)
        {
            System.out.println("Syntax: ConsoleClient <host> <port>");
            System.exit(8);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ConsoleClient client = new ConsoleClient(host, port);
        Thread th = new Thread(client);
        th.setDaemon(false);
        th.start();
    }
}
