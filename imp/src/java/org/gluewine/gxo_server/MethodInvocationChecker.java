/**************************************************************************
 *
 * Gluewine GXO Server Module
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
package org.gluewine.gxo_server;

import java.lang.reflect.Method;

import org.gluewine.gxo.ExecBean;

/**
 * Allows to check whether a method may be invoked.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface MethodInvocationChecker
{
    // ===========================================================================
    /**
     * Checks the method given to see whether it is allowed to be executed.
     * If the implementation finds that the method is not allowed to be executed
     * it should throw an exception.
     *
     * @param clazz The class impacted.
     * @param method The method that will be invoked.
     * @param bean The exec bean.
     * @throws Throwable If the method is not allowed to be executed.
     */
    void checkAllowed(Class<?> clazz, Method method, ExecBean bean) throws Throwable;
}
