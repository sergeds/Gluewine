package org.gluewine.jetty;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The default servlet.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DefaultServlet extends GluewineServlet
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -9058542491907179908L;

    /**
     * The jetty instance.
     */
    private GluewineHandler handler;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param handler The handler instance.
     */
    DefaultServlet(GluewineHandler handler)
    {
        this.handler = handler;
    }

    // ===========================================================================
    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
    {
        final long now = new Date().getTime();
        resp.setDateHeader("Date", now);
        resp.setDateHeader("Expires", now);

        resp.setContentType("text/html");
        StringBuilder b = new StringBuilder("<HTML><HEAD>");
        b.append("<TITLE>Gluewine framework</TITLE>");
        b.append("</HEAD>");
        //
        b.append("<H1>Welcome to the Gluewine framework.</H1>");
        b.append("<p>We're sorry but the context you tried to reach does not seem to exist!");
        b.append("<br>Here's the list of available contexts:");
        b.append("<ul>");

        Set<String> contexts = new TreeSet<String>();
        contexts.addAll(handler.getContexts().keySet());

        for (String s : contexts)
        {
            b.append("<li>");
            b.append("<A HREF='").append(s).append("'>").append(s).append("</A>");
            b.append("</li>");
        }
        b.append("</ul>");
        b.append("</HEAD>");
        resp.setContentLength(b.length());
        try
        {
            resp.getWriter().println(b.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    // ===========================================================================
    @Override
    public String getContextPath()
    {
        return "default";
    }
}