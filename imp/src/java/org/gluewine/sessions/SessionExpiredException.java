/**************************************************************************
 *
 * Gluewine Base Session Management Module
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
package org.gluewine.sessions;

/**
 * Exception that is thrown when a session is no longer valid.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SessionExpiredException extends RuntimeException
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 7032793928687477214L;
}
