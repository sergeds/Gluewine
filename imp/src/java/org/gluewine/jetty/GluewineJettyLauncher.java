package org.gluewine.jetty;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.gluewine.authentication.UseridPasswordAuthentication;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.core.utils.ErrorLogger;

/**
 * Launches the Jerry Server and registers all available contexts.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineJettyLauncher implements RepositoryListener<GluewineServlet>, CommandProvider
{
    // ===========================================================================
    /**
     * The property file to use.
     */
    @Glue(properties = "jetty.properties", refresh = "configRefreshed")
    private Properties properties;

    /**
     * The server instance.
     */
    private Server server = null;

    /**
     * The repository.
     */
    @Glue
    private Repository repository;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The contexts.
     */
    private ContextHandlerCollection contexts = new ContextHandlerCollection();

    /**
     * Map of registered handlers.
     */
    private Map<String, Handler> handlers = new HashMap<String, Handler>();

    // ===========================================================================
    /**
     * Loads all the wars.
     */
    private void loadWars()
    {
        File warDirectory = new File(properties.getProperty("war.directory", "/tmp"));
        if (!warDirectory.exists())
            if (!warDirectory.mkdirs()) logger.warn("Could not create the war directory " + warDirectory.getAbsolutePath());

        File[] wars = warDirectory.listFiles();
        if (wars != null)
        {
            for (final File war : wars)
            {
                if (war.getName().endsWith(".war"))
                {
                    try
                    {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
                        {
                            @Override
                            public Void run() throws Exception
                            {
                                String context = war.getName();
                                int i = context.lastIndexOf(".war");
                                if (i > -1) context = context.substring(0, i);
                                context = "/" + context;

                                WebAppContext webapp = new WebAppContext();

                                WebAppClassLoader wp = new WebAppClassLoader(GluewineJettyLauncher.class.getClassLoader(), webapp);
                                webapp.setClassLoader(wp);
                                webapp.setWar(war.getAbsolutePath());
                                webapp.setSessionHandler(initSessionHandler(context));

                                if (context.equals("/default")) webapp.setContextPath("/");
                                else webapp.setContextPath(context);

                                contexts.addHandler(webapp);
                                return null;
                            }
                        });
                    }
                    catch (PrivilegedActionException e)
                    {
                        ErrorLogger.log(GluewineJettyLauncher.class, e);
                    }
                }
            }
        }

    }

    // ===========================================================================
    /**
     * Stops the server.
     */
    @RunOnDeactivate
    public void stopServer()
    {
        if (server != null)
        {
            try
            {
                logger.info("Stopping Jetty Embedded Server.");
                server.stop();
            }
            catch (Exception e)
            {
                ErrorLogger.log(getClass(), e);
            }
            server = null;
        }
    }

    // ===========================================================================
    /**
     * Launches the Jetty server.
     */
    @RunOnActivate(runThreaded = true)
    public void launch()
    {
        try
        {
            logger.info("Launching Jetty Embedded Server");
            if (properties.containsKey("maxthreads"))
            {
                logger.info("Launching Jetty with a maximum of " + Integer.parseInt(properties.getProperty("maxthreads")) + " concurrent threads!");
                QueuedThreadPool threadPool = new QueuedThreadPool(Integer.parseInt(properties.getProperty("maxthreads")));
                server = new Server(threadPool);
            }
            else
                server = new Server();

            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            logger.info("Loading class " + ServletContextListener.class.getName());

            loadWars();
            loadStaticHandlers();

            // Register the default servlet if no other servlet has been registered as root.
            if (!handlers.containsKey("/")) registered(new DefaultServlet(this));

            GzipHandler gzipHandler = new GzipHandler();
            gzipHandler.setHandler(contexts);

            server.setHandler(gzipHandler);

            // Add SSL configuration if configured:
            if (properties.containsKey("https.port"))
            {
                logger.info("Adding HTTPS connection");
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStorePath(properties.getProperty("https.keystorepath", "../cfg/keystore"));
                sslContextFactory.setKeyStorePassword(properties.getProperty("https.keystorepassword"));
                sslContextFactory.setKeyManagerPassword(properties.getProperty("https.keymanagerpassword"));
                sslContextFactory.setTrustStorePath(properties.getProperty("https.truststorepath", "../cfg/truststore"));
                sslContextFactory.setTrustStorePassword(properties.getProperty("https.truststorepassword"));
                sslContextFactory.setNeedClientAuth(Boolean.parseBoolean(properties.getProperty("https.clientauthentication", "false")));
                sslContextFactory.setTrustAll(true);

                HttpConfiguration https_config = new HttpConfiguration();
                https_config.addCustomizer(new SecureRequestCustomizer());
                // SSL Connector
                ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https_config));
                sslConnector.setPort(Integer.parseInt(properties.getProperty("https.port")));
                server.addConnector(sslConnector);
            }

            ServerConnector http = new ServerConnector(server);
            http.setPort(Integer.parseInt(properties.getProperty("http.port", "8080")));
            server.addConnector(http);

            server.start();
            server.join();
        }
        catch (Throwable e)
        {
            logger.error(e);
        }
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

            String context = properties.getProperty("static." + i + ".context", dir.getName());

            if (properties.getProperty("static." + i + ".secured", "false").equals("true"))
            {
                UseridPasswordAuthentication authenticator = repository.getService(UseridPasswordAuthentication.class);
                if (authenticator != null)
                    handler = new GluewineSecuredStaticHandler(context, authenticator);

                else
                {
                    logger.warn("No Authenticator available!");
                    handler = new GluewineStaticHandler(context);
                }

            }
            else
                handler = new GluewineStaticHandler(context);

            handler.setResourceBase(path);
            handler.setDirectoriesListed(Boolean.parseBoolean(properties.getProperty(pref + ".directoryListing", "false")));
            handler.setWelcomeFiles(properties.getProperty(pref + ".welcome", "index.html").split(","));

            contexts.addHandler(handler);

            i++;
            path = properties.getProperty("static." + i + ".path");
        }
    }

    // ===========================================================================
    /**
     * Initializes and returns the session manager to use for the given context.
     *
     * @param context The context of the session manager.
     * @return The Session Manager.
     * @throws IOException If the store directory could not be set.
     */
    private SessionHandler initSessionHandler(String context) throws IOException
    {
        if (context.startsWith("/")) context = context.substring(1);

        HashSessionManager sessionManager = new HashSessionManager();
        sessionManager.setSessionIdManager(server.getSessionIdManager());

        if (properties.contains(context + ".setCheckingRemoteSessionIdEncoding"))
            sessionManager.setCheckingRemoteSessionIdEncoding(Boolean.parseBoolean(properties.getProperty(context + ".setCheckingRemoteSessionIdEncoding")));
        else if (properties.contains("default.setCheckingRemoteSessionIdEncoding"))
            sessionManager.setCheckingRemoteSessionIdEncoding(Boolean.parseBoolean(properties.getProperty("default.setCheckingRemoteSessionIdEncoding")));

        if (properties.contains(context + ".setDeleteUnrestorableSessions"))
            sessionManager.setDeleteUnrestorableSessions(Boolean.parseBoolean(properties.getProperty(context + ".setDeleteUnrestorableSessions")));
        else if (properties.contains("default.setDeleteUnrestorableSessions"))
            sessionManager.setDeleteUnrestorableSessions(Boolean.parseBoolean(properties.getProperty("default.setDeleteUnrestorableSessions")));

        if (properties.contains(context + ".setHttpOnly"))
            sessionManager.setHttpOnly(Boolean.parseBoolean(properties.getProperty(context + ".setHttpOnly")));
        else if (properties.contains("default.setHttpOnly"))
            sessionManager.setHttpOnly(Boolean.parseBoolean(properties.getProperty("default.setHttpOnly")));

        if (properties.contains(context + ".setIdleSavePeriod"))
            sessionManager.setIdleSavePeriod(Integer.parseInt(properties.getProperty(context + ".setIdleSavePeriod")));
        else if (properties.contains("default.setIdleSavePeriod"))
            sessionManager.setIdleSavePeriod(Integer.parseInt(properties.getProperty("default.setIdleSavePeriod")));

        if (properties.contains(context + ".setLazyLoad"))
            sessionManager.setLazyLoad(Boolean.parseBoolean(properties.getProperty(context + ".setLazyLoad")));
        else if (properties.contains("default.setLazyLoad"))
            sessionManager.setLazyLoad(Boolean.parseBoolean(properties.getProperty("default.setLazyLoad")));

        if (properties.contains(context + ".setMaxInactiveInterval"))
            sessionManager.setMaxInactiveInterval(Integer.parseInt(properties.getProperty(context + ".setMaxInactiveInterval")));
        else if (properties.contains("default.setMaxInactiveInterval"))
            sessionManager.setMaxInactiveInterval(Integer.parseInt(properties.getProperty("default.setMaxInactiveInterval")));

        if (properties.contains(context + ".setNodeIdInSessionId"))
            sessionManager.setNodeIdInSessionId(Boolean.parseBoolean(properties.getProperty(context + ".setNodeIdInSessionId")));
        else if (properties.contains("default.setNodeIdInSessionId"))
            sessionManager.setNodeIdInSessionId(Boolean.parseBoolean(properties.getProperty("default.setNodeIdInSessionId")));

        if (properties.contains(context + ".storeDirectory"))
        {
            File f = new File(context + ".storeDirectory");
            if (!f.exists())
                if (!f.mkdirs()) throw new IOException("Could not create directory " + f.getAbsolutePath());
            sessionManager.setStoreDirectory(f);
        }
        else if (properties.contains("default.storeDirectory"))
        {
            File f = new File("default.storeDirectory");
            if (!f.exists())
                if (!f.mkdirs()) throw new IOException("Could not create directory " + f.getAbsolutePath());
            sessionManager.setStoreDirectory(f);
        }

        if (properties.contains(context + ".setSavePeriod"))
            sessionManager.setSavePeriod(Integer.parseInt(properties.getProperty(context + ".setSavePeriod")));
        else if (properties.contains("default.setSavePeriod"))
            sessionManager.setSavePeriod(Integer.parseInt(properties.getProperty("default.setSavePeriod")));

        if (properties.contains(context + ".setScavengePeriod"))
            sessionManager.setScavengePeriod(Integer.parseInt(properties.getProperty(context + ".setScavengePeriod")));
        else if (properties.contains("default.setScavengePeriod"))
            sessionManager.setScavengePeriod(Integer.parseInt(properties.getProperty("default.setScavengePeriod")));

        if (properties.contains(context + ".setUsingCookies"))
            sessionManager.setUsingCookies(Boolean.parseBoolean(properties.getProperty(context + ".setUsingCookies")));
        else if (properties.contains("default.setUsingCookies"))
            sessionManager.setUsingCookies(Boolean.parseBoolean(properties.getProperty("default.setUsingCookies")));

        return new SessionHandler(sessionManager);
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
        for (Entry<String, Handler> e : handlers.entrySet())
            cc.tableRow(e.getKey(), e.getValue().getClass().getName());

        cc.printTable();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();
        commands.add(new CLICommand("jetty_contexts", "Lists the available contexts."));
        return commands;
    }

    // ===========================================================================
    /**
     * Returns the sets of active contexts.
     *
     * @return The active contexts.
     */
    Set<String> getActiveContexts()
    {
        Set<String> s = new TreeSet<String>();
        s.addAll(handlers.keySet());
        return s;
    }

    // ===========================================================================
    @Override
    public void registered(GluewineServlet t)
    {
        String ctx = t.getContextPath();
        if (!ctx.startsWith("/")) ctx = "/" + ctx;
        int i = ctx.indexOf('/', 1);
        String path = "/*";
        if (i > 1)
        {
            path = ctx.substring(i);
            ctx = ctx.substring(0, i);
        }
        if (!path.endsWith("*")) path = path + "*";

        Handler h = handlers.get(ctx);
        ServletContextHandler handler = null;
        if (h != null && h instanceof ServletContextHandler) handler = (ServletContextHandler) h;
        if (handler == null)
        {
            if (logger.isDebugEnabled()) logger.debug("Creating handler for context " + ctx);
            handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            handler.setContextPath(ctx);
            handlers.put(ctx, handler);
            contexts.addHandler(handler);
        }

        if (logger.isDebugEnabled()) logger.debug("Adding path: " + path + " to context " + ctx);
        ServletHolder holder = new ServletHolder(t);
        handler.addServlet(holder, path);
    }

    // ===========================================================================
    @Override
    public void unregistered(GluewineServlet t)
    {
        String ctx = t.getContextPath();
        if (!ctx.startsWith("/")) ctx = "/" + ctx;
        int i = ctx.indexOf('/', 1);
        if (i > 1) ctx = ctx.substring(0, i);
        Handler h = handlers.remove(ctx);
        if (h != null) contexts.removeHandler(h);
    }
}
