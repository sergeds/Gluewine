/**************************************************************************
 *
 * Gluewine GWT Integration Module
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
package org.gluewine.gwt;

import org.apache.log4j.Logger;
import org.gluewine.gxo_client.GxoClient;
import org.gluewine.sessions.SessionExpiredException;
import org.gluewine.utils.ErrorLogger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Abstract Gluewine Service. This service provides access to Gluewine services either
 * through the use of GXO (in case the service is located in a remote VM), either by
 * accessing the Repository when running in the same VM.
 *
 * Remark: when 'Repository' is used, the services requested are encapsulated by a Proxy,
 * that will handle all 'hibernate' class mapping
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class AbstractGluewineService extends RemoteServiceServlet
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 6999739620740864110L;

    /**
     * The client to use when in GXO mode.
     */
    private static GxoClient gxoClient = null;

    /**
     * Name of the attribute containing the OSGi session id.
     */
    private static final String OSGI_SESSION = "osgi_session";

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    @Override
    public void init()
    {
        synchronized (OSGI_SESSION)
        {
            if (gxoClient == null)
            {
                boolean local = System.getProperty("gluewine.gxolocal") != null;
                if (local)
                {
                    logger.info("Starting servlet " + getClass().getName() + " in 'Local' mode.");
                    gxoClient = new GxoClient();
                }

                else
                {
                    String osgiHost = this.getServletContext().getInitParameter("osgi-host");
                    int osgiPort = Integer.parseInt(this.getServletContext().getInitParameter("osgi-port"));
                    logger.info("Starting servlet " + getClass().getName() + " in 'Remove' mode, connecting to server " + osgiHost + ":" + osgiPort);
                    gxoClient = new GxoClient(osgiHost, osgiPort);
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Returns a proxy to the service of the given class.
     *
     * @param c The service class to access.
     * @param <T> The service class.
     * @return A proxy to that service.
     */
    protected <T> T getService(Class<T> c)
    {
        logger.debug("Requesting service " + c.getName());
        return (T) gxoClient.getService(c, getCurrentOSGiSession(), getInvokerAddress());
    }


    // ===========================================================================
    /**
     * Returns the current OSGi session. If no session is available, -1 is
     * returned.
     *
     * @return The current OSGi session.
     */
    protected String getCurrentOSGiSession()
    {
        String session = "-1";
        Object o = getThreadLocalRequest().getSession().getAttribute(OSGI_SESSION);
        if (o != null && o instanceof String)
            session = (String) o;

        return session;
    }

    // ===========================================================================
    /**
     * Returns the ip address of the invoker.
     *
     * @return The ip address of the invoker.
     */
    protected String getInvokerAddress()
    {
        String address = getThreadLocalRequest().getHeader("X-Forwarded-For");
        if (address == null)
           address = getThreadLocalRequest().getRemoteAddr();

        return address;
    }

    // ===========================================================================
    /**
     * Sets the current OSGi session.
     *
     * @param session The session to set.
     */
    protected void setCurrentOSGiSession(String session)
    {
        getThreadLocalRequest().getSession().setAttribute(OSGI_SESSION, session);
    }

    // ===========================================================================
    /**
     * Clears the current Gluewine session.
     */
    protected void clearCurrentOSGiSession()
    {
        getThreadLocalRequest().getSession().setAttribute(OSGI_SESSION, "-1");
    }

    // ===========================================================================
    /**
     * Returns the client.
     *
     * @return The client to use.
     */
    protected static GxoClient getGXOClient()
    {
        return gxoClient;
    }

    // ===========================================================================
    /**
     * Handles the given runtime exception. If the exception is a SessionExpired
     * exception, the session is cleared, and the exception is thrown.
     *
     * The exception is thrown back and logged in all cases.
     *
     * @param e The exception to handle.
     * @param <T> The generic return signature.
     * @param T Just to fool the compiler.
     */
   protected <T> T handleException(Throwable e)
    {
        if (e instanceof SessionExpiredException)
            clearCurrentOSGiSession();

        ErrorLogger.log(getClass(), e);

        e.printStackTrace();

        if (e instanceof RuntimeException) throw (RuntimeException) e;
        else throw new RuntimeException(e);
    }
}
