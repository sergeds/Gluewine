package org.gluewine.rest_dbauth;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.authentication.AuthenticationException;
import org.gluewine.core.Glue;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.dbauth.DBAuthenticator;
import org.gluewine.rest_server.RESTAuthenticator;
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

    /**
     * The checker that checks session validity.
     */
    private Timer sessionChecker = null;

    // ===========================================================================
    @RunOnActivate
    public void initialize()
    {
        sessionChecker = new Timer();
        sessionChecker.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                synchronized (sessions)
                {
                    Iterator<Entry<String, String>> iter = sessions.entrySet().iterator();
                    while (iter.hasNext())
                    {
                        Entry<String, String> e = iter.next();
                        try
                        {
                            sessionManager.checkSession(e.getValue());
                        }
                        catch (SessionExpiredException t)
                        {
                            iter.remove();
                        }
                    }
                }
            }
        }, 60000, 60000);
    }

    // ===========================================================================
    /**
     * Stops the timer.
     */
    @RunOnDeactivate
    public void stopChecker()
    {
        if (sessionChecker != null)
        {
            sessionChecker.cancel();
            sessionChecker = null;
        }
    }

    // ===========================================================================
    @Override
    public String authenticate(HttpServletRequest req, HttpServletResponse response) throws AuthenticationException
    {
        // First we'll check if there's a valid session id.
        String httpSession = req.getSession(true).getId();
        String session = null;
        synchronized (sessions)
        {
            session = sessions.get(httpSession);
        }

        if (session == null)
            session = req.getHeader("Gluewine-Session");

        if (session != null)
        {
            try
            {
                sessionManager.checkAndTickSession(session);
            }
            catch (SessionExpiredException e)
            {
                // Session has expired.
                synchronized (sessions)
                {
                    sessions.remove(session);
                    session = null;
                }
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
                if (session != null)
                {
                    synchronized (sessions)
                    {
                        sessions.put(httpSession, session);
                    }
                }
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
