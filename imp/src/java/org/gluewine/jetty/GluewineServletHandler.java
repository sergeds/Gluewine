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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Handler that simply services a servlet. The context of the servlet is handled
 * in the GluewineHandler.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineServletHandler extends AbstractHandler
{
    // ===========================================================================
    /**
     * The servlet being handled.
     */
    private HttpServlet servlet = null;

    // ===========================================================================
    /**
     * The servlet to service.
     *
     * @param servlet The servlet.
     */
    public GluewineServletHandler(HttpServlet servlet)
    {
        this.servlet = servlet;
    }

    // ===========================================================================
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        servlet.service(request, response);
        baseRequest.setHandled(true);
    }
}
