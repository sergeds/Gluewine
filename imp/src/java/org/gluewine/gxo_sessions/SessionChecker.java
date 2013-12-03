/**************************************************************************
 *
 * Gluewine GXO Sessions Module
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
