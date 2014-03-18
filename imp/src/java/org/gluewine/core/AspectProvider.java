/**************************************************************************
 *
 * Gluewine Core Module
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
package org.gluewine.core;

import java.lang.reflect.Method;

/**
 * Interface that can be implemented by classes that
 * provide additional behaviour to registered services.
 * (Aspect programming).
 *
 * <br>Whenever such an interface is registered in the OSGi framework, it will
 * be used by the Proxy that has registered the service.
 *
 * <br>Remark that no guarantee can be made to the order of AspectProviders, and hence
 * AspectProviders should no be dependent on each other. (in regards of method invocation that is).
 *
 * <br>The invocation flow is as follows:
 * <ul>
 * <li>The beforeInvocation() is invoked.</li>
 * <li>The actual method is invoked</li>
 * <li>If the method did not throw an exception, the afterSuccess() is invoked.</li>
 * <li>If the method did throw an exception, the afterFailure() is invoked.</li>
 * <li>The after() is invoked</li>
 * </ul>
 *
 * <br>Implementations can choose which method to implement, and leave the others empty.
 *
 * <br>AspectProviders are expected to be thread safe.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface AspectProvider
{
    // ===========================================================================
    /**
     * Invoked before the given method is invoked on the object.
     *
     * @param o The object the method will be invoked on.
     * @param m The method that is going to be invoked.
     * @param params The parameters of the method.
     * @throws Throwable If the method should not be invoked.
     */
    void beforeInvocation(Object o, Method m, Object[] params) throws Throwable;

    // ===========================================================================
    /**
     * Invoked after the given method has been invoked successfully on an object.
     *
     * <br>Successfully means that the invoked method did not throw an exception.
     *
     * @param o The object the method was invoked on.
     * @param m The method that has been invoked.
     * @param params The parameters of the method.
     * @param result The result of the object. (will be null if the method is void)
     */
    void afterSuccess(Object o, Method m, Object[] params, Object result);

    // ===========================================================================
    /**
     * Invoked after the given method that was invoked on the object threw an exception.
     *
     * @param o The object the method was invoked on.
     * @param m The method that has been invoked.
     * @param params The parameters of the method.
     * @param e The throwable that the method threw.
     */
    void afterFailure(Object o, Method m, Object[] params, Throwable e);

    // ===========================================================================
    /**
     * Invoked after the method finished regardless on whether the method did
     * throw an exception or not.
     *
     * @param o The object the method was invoked on.
     * @param m The method that has been invoked.
     * @param params The parameters of the method.
     */
    void after(Object o, Method m, Object[] params);
}
