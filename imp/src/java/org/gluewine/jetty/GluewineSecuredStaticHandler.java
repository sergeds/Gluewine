package org.gluewine.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.gluewine.console.AuthenticationException;
import org.gluewine.dbauth.DBAuthenticator;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * Static content handler that requires authentication.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineSecuredStaticHandler extends GluewineStaticHandler
{
    // ===========================================================================
    /**
     * The authenticator to use.
     */
    private DBAuthenticator authenticator;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param context The context.
     * @param authenticator The authenticator to use.
     */
    public GluewineSecuredStaticHandler(String context, DBAuthenticator authenticator)
    {
        super(context);
        this.authenticator = authenticator;
    }

    // ===========================================================================
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic "))
        {
            String[] up = parseBasic(authHeader.substring(authHeader.indexOf(" ") + 1));
            String username = up[0];
            String password = up[1];
            try
            {
                String session = authenticator.authenticate(username, password);
                if (session != null)
                {
                    super.handle(target, baseRequest, request, response);
                    return;
                }
            }
            catch (AuthenticationException e)
            {
                logger.warn("Invalid login request for user " + username);
            }
        }

        response.setHeader("WWW-Authenticate", "BASIC realm=\"SecureFiles\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please provide username and password");
    }

    // ===========================================================================
    /**
     * Parses the userid and password from the string specified.
     *
     * @param enc The string to process.
     * @return The userid and password.
     */
    private String[] parseBasic(String enc)
    {
        byte[] bytes = new Base64Encoder().decode(enc);
        String s = new String(bytes);
        int pos = s.indexOf(":");
        if (pos >= 0)
            return new String[] {s.substring(0, pos), s.substring(pos + 1)};
        else
            return new String[] {s, null};
    }
}
