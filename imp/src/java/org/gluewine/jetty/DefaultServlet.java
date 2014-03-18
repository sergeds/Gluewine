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
import java.util.Date;

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
    private GluewineJettyLauncher launcher = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param launcher The Jetty launcher.
     */
    DefaultServlet(GluewineJettyLauncher launcher)
    {
        this.launcher = launcher;
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

        for (String s : launcher.getActiveContexts())
        {
            if (s.trim().length() > 1)
            {
                b.append("<li>");
                b.append("<A HREF='").append(s).append("'>").append(s).append("</A>");
                b.append("</li>");
            }
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
        return "/";
    }
}
