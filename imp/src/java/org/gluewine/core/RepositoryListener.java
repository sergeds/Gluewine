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

/**
 * Defines a listener that will be notified every time an object has either been registered or unregistered and that is
 * an instance of the generic type specified.
 *
 * @author fks/Serge de Schaetzen
 *
 * @param <T> The generic class.
 */
public interface RepositoryListener<T extends Object>
{
    // ===========================================================================
    /**
     * Invoked when an instance of the given type has been registered.
     *
     * @param t The class that was registered.
     */
    void registered(T t);

    // ===========================================================================
    /**
     * Invoked when an instance of the given type has been unregistered.
     *
     * @param t The class that was unregistered.
     */
    void unregistered(T t);
}
