/**************************************************************************
 *
 * Gluewine Camel Integration Module
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
package org.gluewine.analytics;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;


/**
 * The GoogleAnalytics allows to send data to Google Analytics for
 * data collection.
 *
 * You can aloso
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GoogleAnalytics implements AspectProvider
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
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Tracks the page with the given properties.
     *
     * @param req The current request.
     * @param hostName The hostname.
     * @param page The page.
     * @param title The page title.
     *
     * @throws IOException If an error occurs.
     */
    public void trackWebPage(HttpServletRequest req, String hostName, String page, String title) throws IOException
    {
        String id = props.getProperty(TRACKID_WEBID);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&t=pageview");
            b.append("&dh=").append(URLEncoder.encode(hostName, "UTF-8"));
            b.append("&dp=").append(URLEncoder.encode(page, "UTF-8"));
            b.append("&dt=").append(URLEncoder.encode(title, "UTF-8"));

            postRequest(req, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Tracks the event with the given properties.
     *
     * @param req The current request.
     * @param category The category name.
     * @param action The action.
     * @param label The label.
     * @param value The value.
     *
     * @throws IOException If an error occurs.
     */
    public void trackWebEvent(HttpServletRequest req, String category, String action, String label, String value) throws IOException
    {
        String id = props.getProperty(TRACKID_WEBID);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&t=event");
            b.append("&ec=").append(URLEncoder.encode(category, "UTF-8"));
            b.append("&ea=").append(URLEncoder.encode(action, "UTF-8"));
            b.append("&el=").append(URLEncoder.encode(label, "UTF-8"));
            b.append("&ev=").append(URLEncoder.encode(value, "UTF-8"));

            postRequest(req, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Tracks a Mobile App screen view.
     *
     * @param req The current request.
     * @param appName The app name.
     * @param appVersion The app version.
     * @param screenName The screen name.
     * @throws IOException If an error ocurrs.
     */
    public void trackMobileScreen(HttpServletRequest req, String appName, String appVersion, String screenName) throws IOException
    {
        String id = props.getProperty(TRACKID_MOBILE);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&t=appview");
            b.append("&an=").append(URLEncoder.encode(appName, "UTF-8"));
            b.append("&av=").append(URLEncoder.encode(appVersion, "UTF-8"));
            b.append("&cd=").append(URLEncoder.encode(screenName, "UTF-8"));

            postRequest(req, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Tracks a Mobile App screen view.
     *
     * @param req The current request.
     * @param appName The app name.
     * @param category The category name.
     * @param action The action.
     * @throws IOException If an error ocurrs.
     */
    public void trackMobileEvent(HttpServletRequest req, String appName, String category, String action) throws IOException
    {
        String id = props.getProperty(TRACKID_MOBILE);
        if (id != null)
        {
            StringBuilder b = new StringBuilder("payload_data&v=1");
            b.append("&tid=").append(id);
            b.append("&cid={CLIENTID}");
            b.append("&t=event");
            b.append("&an=").append(URLEncoder.encode(appName, "UTF-8"));
            b.append("&ec=").append(URLEncoder.encode(category, "UTF-8"));
            b.append("&ea=").append(URLEncoder.encode(action, "UTF-8"));

            postRequest(req, b.toString());
        }

        else throw new IOException("There is no " + TRACKID_WEBID + " defined in the google.properties files!");
    }

    // ===========================================================================
    /**
     * Posts the request with the given parameters to GoolgeAnalytics.
     *
     * @param req The request invoking the trigger.
     * @param parameters The parameters to report.
     * @throws IOException If an error occurs connecting to Google.
     */
    @SuppressWarnings("unchecked")
    private void postRequest(HttpServletRequest req, String parameters) throws IOException
    {
        String target = props.getProperty(URL);
        parameters = parameters.replace("{CLIENTID}", req.getRemoteAddr());
        URL url = new URL(target + "?" + parameters);

        logger.debug("GoogleAnalytics Report : " + url.toExternalForm());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        if (req != null)
        {
            Enumeration<String> names = req.getHeaderNames();
            while (names.hasMoreElements())
            {
                String name = names.nextElement();
                connection.setRequestProperty(name, req.getHeader(name));
                logger.trace("HEADER: " + name + " = " + req.getHeader(name));
            }

            String forward = req.getHeader("X-Forwarded-For");
            if (forward == null)
            {
                forward = req.getRemoteAddr();
                connection.setRequestProperty("X-Forwarded-For", forward);
                logger.trace("HEADER: X-Forwarded-For = " + forward);
            }
        }

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes("utf-8").length));
        connection.setUseCaches(false);
        connection.connect();

        logger.debug("GoogleAnalytics Report : " + url.toExternalForm() + " done with responce code " + connection.getResponseCode());

        connection.disconnect();
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
                        postRequest(req, payload);
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
}
