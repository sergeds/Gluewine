package org.gluewine.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.authentication.AuthenticationException;

/**
 * Defines an authenticator to use for REST requests.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface RESTAuthenticator
{
    // ===========================================================================
    /**
     * Requests the instance to perform authentication using the given request and response.
     * If authentication fails, an exception must be thrown. The response can be used
     * to set some headers, but should not be used to send an error code, as
     * the RESTServlet will handle that.
     *
     * @param req The current request.
     * @param response The current response.
     * @return The session id.
     * @throws AuthenticationException If authentication fails.
     */
    String authenticate(HttpServletRequest req, HttpServletResponse response) throws AuthenticationException;
}
