package org.gluewine.gwt;

import org.apache.log4j.Logger;
import org.gluewine.gxo_client.GxoClient;

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
    private GxoClient gxoClient = null;

    /**
     * Name of the attribute containing the OSGi session id.
     */
    private static final String OSGI_SESSION = "osgi_session";

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance.
     */
    protected AbstractGluewineService()
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
}
