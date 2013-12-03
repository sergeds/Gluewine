package org.gluewine.jetty;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.gluewine.core.Glue;

/**
 * Servlet that allows to reconfigure the Log4j.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Log4jConfigServlet extends GluewineServlet
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -7743875636070940988L;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The properties to use.
     */
    @Glue(properties = "log4j.properties")
    private Properties props;

    // ===========================================================================
    @Override
    public String getContextPath()
    {
        return "log4j";
    }

    // ===========================================================================
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String secToken = req.getParameter("token");
        if (secToken != null)
        {
            String key = props.getProperty("security.token");
            if (key != null && key.equals(secToken))
            {
                PropertyConfigurator.configure(props);
                logger.info("Log4j Reconfigured!");
            }
            else resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not allowed to perform this action!");
        }
        else resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not allowed to perform this action!");
    }
}
