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

import javax.servlet.Servlet;

/**
 * Extends a javax.servlet.Servlet by allowing the servlet to specify a
 * context path.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface GluewineServlet extends Servlet
{
    // ===========================================================================
    /**
     * Returns the context path of this servlet.
     *
     * @return The context path.
     */
    String getContextPath();
}
