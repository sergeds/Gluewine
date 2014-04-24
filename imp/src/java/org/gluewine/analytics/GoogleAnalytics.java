/**************************************************************************
 *
 * Gluewine Camel Integration Module
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
package org.gluewine.analytics;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;


/**
 * The GoogleAnalytics allows to send data to Google Analytics for
 * data collection.
 *
 * You can aloso
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GoogleAnalytics implements AspectProvider, CommandProvider
{
    // ===========================================================================
    /**
     * The properties to use.
     */
    @Glue(properties = "google.properties")
    private Properties props;

    /**
     * The track id property name for web pages.
     */
    private static final String TRACKID_WEBID = "google.analytics.trackid.web";

    /**
     * The track id property name for mobile apps.
     */
    private static final String TRACKID_MOBILE = "google.analytics.trackid.mobile";

    /**
     * The url to connect to.
     */
    private static final String URL = "google.analytics.url";

    /**
     * The logger instance to use.
     */
    private static Logger logger = Logger.getLogger(GoogleAnalytics.class);

    /**
     * The thread pool.
     */
    private ThreadPoolExecutor threadPool = null;

    /**
     * The total events posted.
     */
    private long totalEvents = 0;

    // ===========================================================================
    /**
     * Launches the threadpool.
     */
    @RunOnActivate
    public void launch()
    {
        int max = Integer.parseInt(props.getProperty("threads.max", "10"));
        int idle = Integer.parseInt(props.getProperty("threads.idle", "10"));
        threadPool = new ThreadPoolExecutor(max, max, idle, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        threadPool.allowCoreThreadTimeOut(true);
    }

    // ===========================================================================
    /**
     * Deactivates the service.
     */
    @RunOnDeactivate
    public void deactivate()
    {
        threadPool.shutdown();
    }

    // ===========================================================================
    /**
     * Tracks the page with the given properties.
     *
     * @param headers The headers
     * @param hostName The hostname.
     * @param page The page.
     * @param title The page title.
     *
     * @throws IOException If an error occurs.
     */
    public void trackWebPage(Map<String, String> headers, String hostName, String page, String title) throws IOException
    {
        String id = props.getProperty(TRACKID_WEBID);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&uip={CLIENTID}");
            b.append("&t=pageview");
            b.append("&dh=").append(URLEncoder.encode(hostName, "UTF-8"));
            b.append("&dp=").append(URLEncoder.encode(page, "UTF-8"));
            b.append("&dt=").append(URLEncoder.encode(title, "UTF-8"));

            postRequest(headers, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Submits the event asynchronously.
     *
     * @param headers The headers
     * @param hostName The hostname.
     * @param page The page.
     * @param title The page title.
     */
    public void submitWebPage(final Map<String, String> headers, final String hostName, final String page, final String title)
    {
        threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    trackWebPage(headers, hostName, page, title);
                }
                catch (IOException e)
                {
                    logger.warn(e.getMessage());
                }
            }
        });
    }

    // ===========================================================================
    /**
     * Tracks the event with the given properties.
     *
     * @param headers The headers
     * @param category The category name.
     * @param action The action.
     * @param label The label.
     * @param value The value.
     *
     * @throws IOException If an error occurs.
     */
    public void trackWebEvent(Map<String, String> headers, String category, String action, String label, String value) throws IOException
    {
        String id = props.getProperty(TRACKID_WEBID);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&uip={CLIENTID}");
            b.append("&t=event");
            b.append("&ec=").append(URLEncoder.encode(category, "UTF-8"));
            b.append("&ea=").append(URLEncoder.encode(action, "UTF-8"));
            b.append("&el=").append(URLEncoder.encode(label, "UTF-8"));
            b.append("&ev=").append(URLEncoder.encode(value, "UTF-8"));

            postRequest(headers, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Submits the event asynchronously.
     *
     * @param headers The headers
     * @param category The category name.
     * @param action The action.
     * @param label The label.
     * @param value The value.
     */
    public void submitWebEvent(final Map<String, String> headers, final String category, final String action, final String label, final String value)
    {
        threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    trackWebEvent(headers, category, action, label, value);
                }
                catch (IOException e)
                {
                    logger.warn(e.getMessage());
                }
            }
        });
    }

    // ===========================================================================
    /**
     * Parses the request specified and returns a map containing all header entries.
     *
     * @param req The request to parse.
     * @return The map of header entries.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> parseHeaders(HttpServletRequest req)
    {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements())
        {
            String name = names.nextElement();
            map.put(name, req.getHeader(name));
        }

        map.put("CLIENTID", req.getRemoteAddr());

        String forward = req.getHeader("X-Forwarded-For");
        if (forward != null) map.put("CLIENTID", forward);

        return map;
    }

    // ===========================================================================
    /**
     * Tracks a Mobile App screen view.
     *
     * @param headers The headers
     * @param appName The app name.
     * @param appVersion The app version.
     * @param screenName The screen name.
     * @throws IOException If an error ocurrs.
     */
    public void trackMobileScreen(Map<String, String> headers, String appName, String appVersion, String screenName) throws IOException
    {
        String id = props.getProperty(TRACKID_MOBILE);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&uip={CLIENTID}");
            b.append("&t=appview");
            b.append("&an=").append(URLEncoder.encode(appName, "UTF-8"));
            b.append("&av=").append(URLEncoder.encode(appVersion, "UTF-8"));
            b.append("&cd=").append(URLEncoder.encode(screenName, "UTF-8"));

            postRequest(headers, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_MOBILE + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Submits the event asynchronously.
     *
     * @param headers The headers
     * @param appName The app name.
     * @param appVersion The app version.
     * @param screenName The screen name.
     */
    public void submitMobileScree(final Map<String, String> headers, final String appName, final String appVersion, final String screenName)
    {
        threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    trackMobileScreen(headers, appName, appVersion, screenName);
                }
                catch (IOException e)
                {
                    logger.warn(e.getMessage());
                }
            }
        });
    }


    // ===========================================================================
    /**
     * Tracks a Mobile App screen view.
     *
     * @param headers The headers
     * @param appName The app name.
     * @param category The category name.
     * @param action The action.
     * @throws IOException If an error ocurrs.
     */
    public void trackMobileEvent(Map<String, String> headers, String appName, String category, String action) throws IOException
    {
        String id = props.getProperty(TRACKID_MOBILE);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&uip={CLIENTID}");
            b.append("&t=event");
            b.append("&an=").append(URLEncoder.encode(appName, "UTF-8"));
            b.append("&ec=").append(URLEncoder.encode(category, "UTF-8"));
            b.append("&ea=").append(URLEncoder.encode(action, "UTF-8"));

            postRequest(headers, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_MOBILE + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Submits the event asynchronously.
     *
     * @param headers The headers
     * @param appName The app name.
     * @param category The category name.
     * @param action The action.
     */
    public void submitMobileEvent(final Map<String, String> headers, final String appName, final String category, final String action)
    {
        threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    trackMobileEvent(headers, appName, category, action);
                }
                catch (IOException e)
                {
                    logger.warn(e.getMessage());
                }
            }
        });
    }

    // ===========================================================================
    /**
     * Posts the request with the given parameters to GoolgeAnalytics.
     *
     * @param headers The headers to use..
     * @param parameters The parameters to report.
     * @throws IOException If an error occurs connecting to Google.
     */
    private void postRequest(Map<String, String> headers, String parameters) throws IOException
    {
        String target = props.getProperty(URL);
        parameters = parameters.replace("{CLIENTID}", headers.get("CLIENTID"));
        URL url = new URL(target + "?" + parameters);

        logger.debug("GoogleAnalytics Report : " + url.toExternalForm());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        for (Entry<String, String> e : headers.entrySet())
        {
            connection.setRequestProperty(e.getKey(), e.getValue());
            logger.trace("HEADER: " + e.getKey() + " = " + e.getValue());
        }

        String forward = headers.get("X-Forwarded-For");
        if (forward == null)
        {
            forward = headers.get("CLIENTID");
            connection.setRequestProperty("X-Forwarded-For", forward);
            logger.trace("HEADER: X-Forwarded-For = " + forward);
        }

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes("utf-8").length));
        connection.setUseCaches(false);
        connection.connect();

        logger.debug("GoogleAnalytics Report : " + url.toExternalForm() + " done with responce code " + connection.getResponseCode());

        connection.disconnect();

        synchronized (this)
        {
            totalEvents++;
        }
    }

    // ===========================================================================
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        if (o instanceof HttpServlet)
        {
            // Check if the object and method have been registered to be 'Analyzed'.
            String id = o.getClass().getSuperclass().getName() + "." + m.getName();
            String key = props.getProperty(id);
            if (key != null)
            {
                if (logger.isTraceEnabled()) logger.trace("AfterSucces for " + o.getClass().getSuperclass().getName() + "." + m.getName());
                String payload = props.getProperty(key);
                if (payload != null)
                {
                    // Locate the HttpServletRequest parameter:
                    HttpServletRequest req = null;
                    for (int i = 0; i < params.length && req == null; i++)
                    {
                        if (params[i] instanceof HttpServletRequest)
                            req = (HttpServletRequest) params[i];
                    }
                    try
                    {
                        postRequest(parseHeaders(req), payload);
                    }
                    catch (Throwable e)
                    {
                        logger.error(e);
                    }
                }

                else logger.warn("There is no payload defined for " + key + " in the google.properties file.");
            }
        }
    }

    // ===========================================================================
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
    }

    // ===========================================================================
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
    }

    // ===========================================================================
    @Override
    public void after(Object o, Method m, Object[] params)
    {
    }

    // ===========================================================================
    /**
     * Executes the google_stats command.
     *
     * @param cc The current context.
     */
    public void _google_stats(CommandContext cc)
    {
        cc.tableHeader("Statistic", "Value");
        cc.tableRow("# Total Submits", Long.toString(totalEvents));
        cc.tableRow("# Max Threads", Integer.toString(threadPool.getMaximumPoolSize()));
        cc.tableRow("# Active Threads", Integer.toString(threadPool.getPoolSize()));
        cc.tableRow("# Active Jobs", Integer.toString(threadPool.getActiveCount()));
        cc.tableRow("# Pending jobs", Integer.toString(threadPool.getQueue().size()));

        cc.printTable();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> cmds = new ArrayList<CLICommand>();
        cmds.add(new CLICommand("google_stats", "Displays the Google Analytics statistics."));
        return cmds;
    }
}
