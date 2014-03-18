/**************************************************************************
 *
 * Gluewine GXO Server Module
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
     * @param o The object to inspect..
     * @param method The method that will be invoked.
     * @param bean The exec bean.
     * @throws Throwable If the method is not allowed to be executed.
     */
    void checkAllowed(Object o, Method method, ExecBean bean) throws Throwable;
}
