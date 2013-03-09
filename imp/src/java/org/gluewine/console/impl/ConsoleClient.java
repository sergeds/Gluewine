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

import java.io.File;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jline.Completor;
import jline.ConsoleReader;

import org.gluewine.console.ConsoleServer;
import org.gluewine.console.SyntaxException;
import org.gluewine.gxo_client.GxoClient;

/**
 * Starts the console client.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class ConsoleClient implements Runnable, Completor
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
            server = client.getService(ConsoleServer.class, "-1", getIpAddress());

            ConsoleReader reader = new ConsoleReader();
            if (server.needsAuthentication()) authenticate(reader);

            boolean stopRequested = false;
            reader.setBellEnabled(false);
            String home = System.getProperty("user.home");
            reader.getHistory().clear();
            reader.getHistory().setHistoryFile(new File(home, ".osgi_history"));
            reader.getHistory().setMaxSize(50);
            reader.addCompletor(this);

            while (!stopRequested)
            {
                String line = reader.readLine(prompt);
                if (line != null && (line.startsWith("exit") || line.startsWith("close"))) stopRequested = true;

                else
                {
                    try
                    {
                        String output = server.executeCommand(line);
                        System.out.println(output);
                    }
                    catch (SyntaxException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    catch (Throwable e)
                    {
                        if (e instanceof ConnectException)
                            System.out.println("Connection lost!");

                        else e.printStackTrace();
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
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
