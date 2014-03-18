/**************************************************************************
 *
 * Gluewine REST Server Module
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
package org.gluewine.rest_server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.authentication.AuthenticationException;

/**
 * Defines an authenticator to use for REST requests.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface RESTAuthenticator
{
    // ===========================================================================
    /**
     * Requests the instance to perform authentication using the given request and response.
     * If authentication fails, an exception must be thrown. The response can be used
     * to set some headers, but should not be used to send an error code, as
     * the RESTServlet will handle that.
     *
     * @param req The current request.
     * @param response The current response.
     * @return The session id.
     * @throws AuthenticationException If authentication fails.
     */
    String authenticate(HttpServletRequest req, HttpServletResponse response) throws AuthenticationException;
}
