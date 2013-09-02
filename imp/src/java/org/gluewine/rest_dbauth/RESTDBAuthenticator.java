package org.gluewine.rest_dbauth;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.authentication.AuthenticationException;
import org.gluewine.core.Glue;
import org.gluewine.dbauth.DBAuthenticator;
import org.gluewine.rest.RESTAuthenticator;
import org.gluewine.sessions.SessionExpiredException;
import org.gluewine.sessions.SessionManager;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * Authenticator that delelgates the request to the DBAuthenticator.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class RESTDBAuthenticator implements RESTAuthenticator
{
    // ===========================================================================
    /**
     * The actual authenticator.
     */
    @Glue
    private DBAuthenticator authenticator;

    /**
     * The session manager to use.
     */
    @Glue
    private SessionManager sessionManager;

    /**
     * Map of GluewineSession bound with HTTP Sessions.
     */
    private Map<String, String> sessions = new HashMap<String, String>();

    // ===========================================================================
    @Override
    public String authenticate(HttpServletRequest req, HttpServletResponse response) throws AuthenticationException
    {
        // First we'll check if there's a valid session id.
        String httpSession = req.getSession(true).getId();
        String session = sessions.get(httpSession);
        if (session != null)
        {
            try
            {
                sessionManager.checkAndTickSession(session);
            }
            catch (SessionExpiredException e)
            {
                // Session has expired.
                sessions.remove(session);
                session = null;
            }
        }

        if (session == null)
        {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic "))
            {
                String[] up = parseBasic(authHeader.substring(authHeader.indexOf(" ") + 1));
                String username = up[0];
                String password = up[1];
                session = authenticator.authenticate(username, password);
                if (session != null) sessions.put(httpSession, session);
                else throw new AuthenticationException("Could not obtain a valid session id!");
            }
        }

        if (session == null) throw new AuthenticationException("Authentication Required!");

        return session;
    }

    // ===========================================================================
    /**
     * Parses the userid and password from the string specified.
     *
     * @param enc The string to process.
     * @return The userid and password.
     * @throws AuthenticationException If decoding fails.
     */
    private String[] parseBasic(String enc) throws AuthenticationException
    {
        try
        {
            byte[] bytes = new Base64Encoder().decode(enc);
            String s = new String(bytes, "utf8");
            int pos = s.indexOf(":");
            if (pos >= 0)
                return new String[] {s.substring(0, pos), s.substring(pos + 1)};
            else
                return new String[] {s, null};
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AuthenticationException(e.getMessage());
        }
    }
}
