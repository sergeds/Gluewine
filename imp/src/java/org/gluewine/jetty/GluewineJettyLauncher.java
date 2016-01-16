/**************************************************************************
 *
 * Gluewine Jetty Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gluewine.jetty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.Origin;
import org.gluewine.authentication.UseridPasswordAuthentication;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunAfterRegistration;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.utils.ErrorLogger;

/**
 * Launches the Jerry Server and registers all available contexts and filters.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineJettyLauncher implements RepositoryListener<Object>, CommandProvider, GluewineServletProperties
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

    /**
     * Set of registered Filters.
     */
    private Set<Filter> filters = new HashSet<Filter>();

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

                            String propPrefix = "context." + context.substring(1) + ".";
                            for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
                            {
                                String prop = (String) e.nextElement();
                                if (prop.startsWith(propPrefix))
                                {
                                    String paramName = prop.substring(propPrefix.length());
                                    String paramValue = properties.getProperty(prop);
                                    logger.debug("Setting context parameter " + paramName + " for context " + context + " to " + paramValue);
                                    String prev = webapp.setInitParameter(paramName, paramValue);
                                    logger.debug("Previous value was " + prev);
                                    webapp.getMetaData().setOrigin("context-param." + paramName, Origin.API);

                                }
                            }

                            if (context.equals("/default")) webapp.setContextPath("/");
                            else webapp.setContextPath(context);
                            handlers.put(context, webapp);
                            contexts.addHandler(webapp);
                            addFilters(webapp);
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

    /** Looks for an entry with the specified name in all war files, and returns an InputStream for the entry.
     * @param fileInWar the entry to look for.
     * @return an InputStream for the entry.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "OS_OPEN_STREAM") // We know about this, but it's very hard to fix
    public InputStream getWarContent(String fileInWar)
    {
        for (Handler h : handlers.values())
        {
            if (h instanceof WebAppContext)
            {
                try
                {
                    String war = ((WebAppContext) h).getWar();
                    if (war == null)
                        continue;
                    JarFile jf = new JarFile(war);
                    for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();)
                    {
                        JarEntry je = e.nextElement();
                        if (je.getName().endsWith(fileInWar))
                        {
                            // TODO: This is a problem, because we can't close the war file
                            return jf.getInputStream(je);
                        }
                    }
                }
                catch (Throwable t)
                {
                    ErrorLogger.log(GluewineJettyLauncher.class, t);
                }
            }
        }
        return null;
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
     * Loads the servlets and wars.
     */
    @RunOnActivate
    public void configure()
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

        }
        catch (Throwable e)
        {
            logger.error(e);
        }
    }

    // ===========================================================================
    /**
     * Launches the Jetty server.
     */
    @RunAfterRegistration(runThreaded = true)
    public void launch()
    {
        try
        {
            loadWars();
            loadStaticHandlers();

            boolean registerDefault = properties.getProperty("registerDefaultServlet", "true").equals("true");
            // Register the default servlet if no other servlet has been registered as root.
            if (!handlers.containsKey("/") && registerDefault) registered(new DefaultServlet(this));

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

                HttpConfiguration httpsConfig = new HttpConfiguration();
                httpsConfig.addCustomizer(new SecureRequestCustomizer());
                // SSL Connector
                ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig));
                sslConnector.setPort(Integer.parseInt(properties.getProperty("https.port")));
                sslConnector.setHost(properties.getProperty("https.host"));
                server.addConnector(sslConnector);
            }

            ServerConnector http = new ServerConnector(server);
            http.setPort(Integer.parseInt(properties.getProperty("http.port", "8080")));
            http.setHost(properties.getProperty("http.host"));
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
            if (!context.startsWith("/")) context = "/" + context;

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
            ContextHandler h = new ContextHandler(context);
            h.setHandler(handler);
            handlers.put(context, h);
            contexts.addHandler(h);
            addFilters(h);

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
    /**
     * Registers a servlet with the given context path.
     *
     * @param ctx The context path.
     * @param servlet The servlet to register.
     * @param initParameters Initialization parameters to add to the servlet holder. (may be null)
     */
    public void register(String ctx, Servlet servlet, Map<String, String> initParameters)
    {
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
            if (initParameters.containsKey(RESOURCE_BASE)) handler.setResourceBase(initParameters.get(RESOURCE_BASE));
            handlers.put(ctx, handler);
            contexts.addHandler(handler);
            addFilters(handler);
        }

        if (logger.isDebugEnabled()) logger.debug("Adding path: " + path + " to context " + ctx);
        ServletHolder holder = new ServletHolder((Servlet) servlet);
        if (initParameters != null) holder.setInitParameters(initParameters);

        handler.addServlet(holder, path);
    }

    // ===========================================================================
    /**
     * Unregisters the context specified.
     *
     * @param ctx The context to unregister.
     */
    public void unregister(String ctx)
    {
        if (!ctx.startsWith("/")) ctx = "/" + ctx;
        int i = ctx.indexOf('/', 1);
        if (i > 1) ctx = ctx.substring(0, i);
        Handler h = handlers.remove(ctx);
        if (h != null) contexts.removeHandler(h);
    }

    // ===========================================================================
    @Override
    public void registered(Object t)
    {
        if (t instanceof GluewineServlet)
        {
            GluewineServlet s = (GluewineServlet) t;
            String ctx = s.getContextPath();
            register(ctx, s, s.getInitParameters());
        }
        if (t instanceof Filter)
        {
            Filter f = (Filter) t;
            filters.add(f);
            for (Handler h: handlers.values())
            {
                if (h instanceof ServletContextHandler)
                {
                    ServletHandler sh = ((ServletContextHandler) h).getServletHandler();
                    sh.addFilterWithMapping(new FilterHolder(f), "/", FilterMapping.DEFAULT);
                }
            }
        }
    }

    /**
     * Add already registered filters to a handler.
     * @param h the handler.
     */
    private void addFilters(Handler h)
    {
        if (h instanceof ServletContextHandler)
        {
            ServletHandler sh = ((ServletContextHandler) h).getServletHandler();
            for (Filter f: filters)
                sh.addFilterWithMapping(new FilterHolder(f), "/", FilterMapping.DEFAULT);
        }
    }

    // ===========================================================================
    @Override
    public void unregistered(Object t)
    {
        if (t instanceof GluewineServlet)
        {
            GluewineServlet s = (GluewineServlet) t;
            String ctx = s.getContextPath();
            unregister(ctx);
        }
        if (t instanceof Filter)
        {
            Filter f = (Filter) t;
            filters.remove(f);
            for (Handler h: handlers.values())
            {
                if (h instanceof ServletContextHandler)
                {
                    ServletHandler sh = ((ServletContextHandler) h).getServletHandler();
                    FilterHolder[] holders = sh.getFilters();
                    ArrayList<FilterHolder> hl = new ArrayList<FilterHolder>();
                    for (FilterHolder fh: holders)
                    {
                        if (fh.getFilter() != f)
                        {
                            hl.add(fh);
                        }
                    }
                    sh.setFilters(hl.toArray(new FilterHolder[0]));
                }
            }
        }
    }
}
