/**************************************************************************
 *
 * Gluewine Jetty Integration Module
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
package org.gluewine.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * ContextHandler that allows to map different handlers with a context.
 * Requests are dispatched to the handler that has been registered with that context.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineHandler extends ContextHandlerCollection
{
    // ===========================================================================
    /**
     * Map of available handlers indexed on their context path.
     */
    private Map<String, Handler> handlers = new HashMap<String, Handler>();

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        String context = parseContext(baseRequest.getRequestURI());

        logger.debug("Request recieved for context " + context);

        if (handlers.containsKey(context))
        {
            Handler handler = handlers.get(context);
            logger.debug("Dispatching request to " + handler.getClass().getName());
            handler.handle(target, baseRequest, req, resp);
        }
        else
        {
            logger.debug("No handler found for context " + context);
            resp.getWriter().println("Gluewine Embedded Jetty");
            resp.getWriter().println("There is no resource associated with context path : " + context);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
        }
    }

    // ===========================================================================
    /**
     * Returnst the map of available contexts.
     *
     * @return The map of contexts.
     */
    public Map<String, Handler> getContexts()
    {
        Map<String, Handler> m = new HashMap<>(handlers.size());
        m.putAll(handlers);
        return m;
    }

    // ===========================================================================
    /**
     * Parses and returns the context.
     *
     * @param c The String to process.
     * @return The context.
     */
    private String parseContext(String c)
    {
        if (c.startsWith("/")) c = c.substring(1);
        int i = c.indexOf('/');
        if (i > 0) c = c.substring(0, i);
        return c;
    }

    // ===========================================================================
    /**
     * Adds a handler.
     *
     * @param context The context of the handler.
     * @param handler The handler.
     */
    public void addHandler(String context, Handler handler)
    {
        context = parseContext(context);
        logger.debug("Adding handler: " + handler.getClass().getName() + " for context: " + context);
        handlers.put(context, handler);
        setHandlers(handlers.values().toArray(new Handler[handlers.size()]));
    }

    // ===========================================================================
    /**
     * Removes the context.
     *
     * @param context The context to remove.
     */
    public void removeContext(String context)
    {
        context = parseContext(context);
        logger.debug("Remvoving context: " + context);
        handlers.remove(context);
        setHandlers(handlers.values().toArray(new Handler[handlers.size()]));
    }
}
