/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;

/**
 * Exception used on the client side, used to encapsulate any
 * exception that is returned from the server side.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GxoException extends Exception
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 3387894417989400528L;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param message The reason of the exception.
     */
    public GxoException(String message)
    {
        super(message);
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param cause The reason of the exception.
     */
    public GxoException(Throwable cause)
    {
        super(cause);
    }
}
