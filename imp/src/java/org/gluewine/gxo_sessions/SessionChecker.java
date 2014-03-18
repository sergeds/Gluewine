/**************************************************************************
 *
 * Gluewine GXO Sessions Module
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
package org.gluewine.gxo_sessions;

import java.lang.reflect.Method;

import org.gluewine.core.Glue;
import org.gluewine.gxo.ExecBean;
import org.gluewine.gxo_server.MethodInvocationChecker;
import org.gluewine.sessions.SessionManager;
import org.gluewine.sessions.Unsecured;
import org.gluewine.utils.AnnotationUtility;

/**
 * Checks that the current session is still valid, except if the
 * method is marked as being @UNSECURED.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SessionChecker implements MethodInvocationChecker
{
    // ===========================================================================
    /**
     * The session manager instance to use.
     */
    @Glue
    private SessionManager sessionManager = null;

    // ===========================================================================
    @Override
    public void checkAllowed(Object o, Method method, ExecBean bean) throws Throwable
    {
        if (AnnotationUtility.getAnnotation(Unsecured.class, method, o) == null)
            sessionManager.checkAndTickSession(bean.getSessionId());
    }
}
