/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence;

/**
 * An exception that is thrown when a persistence problem occurs.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class PersistenceException extends RuntimeException
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -2238338822835353599L;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The error message.
     */
    public PersistenceException(String msg)
    {
        super(msg);
    }
}
