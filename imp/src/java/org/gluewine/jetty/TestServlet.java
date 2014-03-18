/**************************************************************************
 *
 * Gluewine Jetty Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
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
