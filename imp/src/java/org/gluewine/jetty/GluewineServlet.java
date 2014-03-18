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

import javax.servlet.http.HttpServlet;

/**
 * Extends a javax.servlet.Servlet by allowing the servlet to specify a
 * context path.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class GluewineServlet extends HttpServlet
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 6457889733711226018L;

    // ===========================================================================
    /**
     * Returns the context path of this servlet.
     *
     * @return The context path.
     */
    public abstract String getContextPath();
}
