/**************************************************************************
 *
 * Gluewine Jetty Integration Module
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
package org.gluewine.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.dbauth.DBAuthenticator;
import org.gluewine.launcher.Launcher;

/**
 * Will launch a Jetty server and deploy all WAR files found.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineJettyLauncher implements CommandProvider, RepositoryListener<GluewineServlet>
{
    // ===========================================================================
    /**
     * The property file to use.
     */
    @Glue(properties = "jetty.properties", refresh = "configRefreshed")
    private Properties properties;

    /**
     * The reference to the base launcher, used to obtain the lib.directory.
     */
    @Glue
    private Launcher launcher;

    @Glue
    private Repository repository;

    /**
     * The server instance.
     */
    private Server server = null;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The directory where to put the war files.
     */
    private File warDirectory = null;

    /**
     * The base handler to use.
     */
    private GluewineHandler baseHandler = new GluewineHandler();

    // ===========================================================================
    /**
     * Launches the server.
     *
     * @throws Exception If an error occurs.
     */
    @RunOnActivate
    public void launch() throws Exception
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.currentThread().setContextClassLoader(GluewineJettyLauncher.class.getClassLoader());

                    logger.info("Loading class " + ServletContextListener.class.getName());

                    int port = Integer.parseInt(properties.getProperty("jetty.port", "8080"));

                    warDirectory = new File(properties.getProperty("war.directory", "/tmp"));
                    if (!warDirectory.exists())
                        if (!warDirectory.mkdirs()) logger.warn("Could not create the war directory " + warDirectory.getAbsolutePath());

                    File[] wars = warDirectory.listFiles();
                    if (wars != null)
                    {
                        for (File war : wars)
                        {
                            if (war.getName().endsWith(".war"))
                                deployWar(war);
                        }
                    }

                    loadStaticHandlers();

                    server = new Server(port);
                    server.setHandler(baseHandler);
                    server.start();
                    server.join();
                }
                catch (Throwable e)
                {
                    logger.error(e);
                }
            }
        }, "JettyLauncher").start();
    }

    // ===========================================================================
    /**
     * Loads the static handlers.
     */
    private void loadStaticHandlers()
    {
        int i = 0;
        String path = properties.getProperty("static." + i + ".path");
        while (path != null)
        {
            File dir = new File(path);
            if (!dir.exists())
            {
                if (!dir.mkdirs())
                    logger.warn("Could not create directory " + dir.getAbsolutePath());
            }

            String pref = "static." + i;

            ResourceHandler handler = null;

            if (properties.getProperty("static." + i + ".secured", "false").equals("true"))
            {
                DBAuthenticator authenticator = repository.getService(DBAuthenticator.class);
                if (authenticator != null)
                    handler = new GluewineSecuredStaticHandler(dir.getName(), authenticator);

                else
                {
                    logger.warn("No Authenticator available!");
                    handler = new GluewineStaticHandler(dir.getName());
                }

            }
            else
                handler = new GluewineStaticHandler(dir.getName());

            handler.setResourceBase(path);
            handler.setDirectoriesListed(Boolean.parseBoolean(properties.getProperty(pref + ".directoryListing", "false")));
            handler.setWelcomeFiles(properties.getProperty(pref + ".welcome", "index.html").split(","));

            baseHandler.addHandler(dir.getName(), handler);

            i++;
            path = properties.getProperty("static." + i + ".path");
        }
    }

    // ===========================================================================
    /**
     * Invoked when the config file has been refreshed.
     *
     * @throws Exception Thrown if an error occurs.
     */
    public void configRefreshed() throws Exception
    {
        stop();
        launch();
    }

    // ===========================================================================
    /**
     * Stops the jetty server.
     *
     * @throws Exception If an error occurs.
     */
    @RunOnDeactivate
    public void stop() throws Exception
    {
        server.stop();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();

        commands.add(new CLICommand("jetty_contexts", "Lists the available contexts."));

        CLICommand cmd = new CLICommand("jetty_deploy", "Deploys a war.");
        cmd.addOption(new CLIOption("-war", "The war file", true, true));
        cmd.addOption(new CLIOption("-context", "The context", true, true));
        commands.add(cmd);

        cmd = new CLICommand("jetty_undeploy", "Undeploys a context.");
        cmd.addOption(new CLIOption("-context", "The context", true, true));
        commands.add(cmd);

        return commands;
    }

    // ===========================================================================
    /**
     * Executes the jetty_contexts command.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _jetty_contexts(CommandContext cc) throws Throwable
    {
        cc.tableHeader("Context", "Handler");
        for (Entry<String, Handler> e : baseHandler.getContexts().entrySet())
            cc.tableRow(e.getKey(), e.getValue().getClass().getName());

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Returns the set of available contexts.
     *
     * @return The set of contexts.
     */
    Set<String> getContexts()
    {
       Set<String> sorted = new TreeSet<String>();
       sorted.addAll(baseHandler.getContexts().keySet());
       return sorted;
    }

    // ===========================================================================
    /**
     * Undeploys a context.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _jetty_undeploy(CommandContext cc) throws Throwable
    {
        String context = cc.getOption("-context");
        if (context.startsWith("/")) context = context.substring(1);
        undeploy(context);
    }

    // ===========================================================================
    /**
     * Fetches the war specified for the given context.
     *
     * @param urlString The url of the war to fetch.
     * @param context The context.
     * @return The fetched war file.
     * @throws Throwable If an error occurs.
     */
    private File fetchWar(String urlString, String context) throws Throwable
    {
        URL url = new URL(urlString);
        File target = new File(warDirectory, context + ".war");

        if (target.exists())
            if (!target.delete()) throw new IOException("Could not delete the war file " + target.getAbsolutePath());

        byte[] buffer = new byte[65536];
        InputStream in = null;
        OutputStream out = null;

        logger.debug("Fetching war " + urlString + " to " + target.getAbsolutePath());

        try
        {
            in = url.openStream();
            out = new FileOutputStream(target);
            int read = in.read(buffer);
            while (read > -1)
            {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
            out.flush();
        }
        finally
        {
            try
            {
                if (in != null) in.close();
            }
            finally
            {
                if (out != null) out.close();
            }
        }
        logger.trace("Target war " + target.getAbsolutePath() + " size: " + target.length());
        return target;
    }

    // ===========================================================================
    /**
     * Deploys a war file.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _jetty_deploy(CommandContext cc) throws Throwable
    {
        String con = cc.getOption("-context");
        if (con.startsWith("/")) con = con.substring(1);

        String war = cc.getOption("-war");

        if (!war.startsWith("http://") && !war.startsWith("file:"))
            war = "file:/" + war;

        File file = fetchWar(war, con);
        deployWar(file);
    }

    // ===========================================================================
    /**
     * Undeploys the given context.
     *
     * @param context The context to undeploy.
     * @throws IOException If the war could not be deployed.
     */
    private void undeploy(String context) throws IOException
    {
        logger.info("Undeploying context " + context);
        baseHandler.removeContext(context);
        File target = new File(warDirectory, context + ".war");
        if (!target.delete()) throw new IOException("Could not delete " + target.getAbsolutePath());
    }

    // ===========================================================================
    /**
     * Deploys the given war in the context specified. The context used will be the
     * name of the file, without path and extension.
     *
     * @param war The war to deploy.
     * @throws IOException If an error occurs.
     */
    public void deployWar(File war) throws IOException
    {
        if (!war.exists()) throw new IOException("The war file " + war.getAbsolutePath() + " does not exist.");
        String context = war.getName();
        int i = context.lastIndexOf(".war");
        if (i > -1) context = context.substring(0, i);

        context = "/" + context;
        deployWar(war, context);
    }

    // ===========================================================================
    /**
     * Deploys the war specified in the given context.
     *
     * @param war The file to deploy;
     * @param context The context to deploy into.
     * @throws IOException If an error occurs reading the file.
     */
    public void deployWar(final File war, final String context) throws IOException
    {
        try
        {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
            {
                @Override
                public Void run() throws Exception
                {
                    WebAppContext webapp = new WebAppContext();
                    WebAppClassLoader wp = new WebAppClassLoader(getClass().getClassLoader(), webapp);
                    webapp.setClassLoader(wp);
                    webapp.setWar(war.getAbsolutePath());

                    logger.info("Deploying war " + war.getAbsolutePath() + " in context " + context);

                    if (context.equals("/default"))
                    {
                        webapp.setContextPath("/");
                        baseHandler.setDefaultHandler(webapp);
                    }
                    else
                    {
                        webapp.setContextPath(context);
                        baseHandler.addHandler(context, webapp);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    public void registered(GluewineServlet t)
    {
        String context = t.getContextPath();
        if (!context.startsWith("/")) context = "/" + context;
        logger.info("Deploying servlet " + t.getClass().getName() + " in context " + context);

        if (context.equals("/default")) baseHandler.setDefaultHandler(new GluewineServletHandler(t));
        else baseHandler.addHandler(context, new GluewineServletHandler(t));
    }

    // ===========================================================================
    @Override
    public void unregistered(GluewineServlet t)
    {
        try
        {
            undeploy(t.getContextPath());
        }
        catch (IOException e)
        {
            logger.warn(e);
        }
    }
}
