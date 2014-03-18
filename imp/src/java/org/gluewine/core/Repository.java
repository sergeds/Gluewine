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
 * Defines the framework repository service.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Repository
{
    // ===========================================================================
    /**
     * Registers the given object with the framework.
     *
     * @param o The object to register.
     */
    void register(Object o);

    // ===========================================================================
    /**
     * Removes the given object from the framework.
     * @param o The object to unregister.
     */
    void unregister(Object o);

    // ===========================================================================
    /**
     * Adds a listener.
     *
     * @param listener The listener to add.
     */
    void addListener(RepositoryListener<?> listener);

    // ===========================================================================
    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    void removeListener(RepositoryListener<?> listener);

    // ===========================================================================
    /**
     * Returns an instance of the given type, or null if no such instance is available.
     *
     * @param <T> The generic type of the service.
     * @param c The type of the instance to return.
     * @return The (possibly null) instance.
     */
    <T> T getService(Class<T> c);
}
