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
 * Exception thrown to indicate that authentication has been aborted,
 * and the console should exit.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class AuthenticationAbortedException extends Exception
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -3586833959468366016L;
}
