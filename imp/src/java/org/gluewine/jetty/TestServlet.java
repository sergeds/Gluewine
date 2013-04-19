/**************************************************************************
 *
 * Gluewine Camel Integration Module
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TestServet voor Jetty.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class TestServlet extends GluewineServlet
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 1066361063768379586L;

    // ===========================================================================
    @Override
    public String getContextPath()
    {
        return "/test";
    }

    // ===========================================================================
    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        try
        {
            resp.getWriter().println("Welcome to the Gluewine Test Servlet.");
        }
        catch (IOException e)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
