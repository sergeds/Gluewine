/**************************************************************************
 *
 * Gluewine Authentication Module
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
package org.gluewine.authentication;

/**
 * Defines an authenticator.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Authenticator
{
    // ===========================================================================
    /**
     * Returns the name of the authenticator.
     *
     * @return The name of the authenticator.
     */
    String getAuthenticatorName();

    // ===========================================================================
    /**
     * Returns the 'base' class name of the authenticator.
     * Note that the class is the 'base' name of the authenticator.
     * The actual class must be named:
     * <ul>
     * <li>- clazz + TxtClient</li>
     * <li>- clazz + GwtClient</li>
     * <li>- clazz + AwtClient</li>
     * </ul>
     * depending on which console client is running.
     * @return The name of the authenticator calss.
     */
    String getAuthenticatorClassName();
}
