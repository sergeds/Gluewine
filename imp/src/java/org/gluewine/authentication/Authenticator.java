/**************************************************************************
 *
 * Gluewine Authentication Module
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
