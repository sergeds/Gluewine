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
package org.gluewine.persistence_jpa;

import org.gluewine.persistence.TransactionCallback;


/**
 * Provides access to TransactionalSessions.
 *
 * TransactionalSessions are automatically committed when the first method
 * in the chain has finished, and automatically rolled back if one of the
 * method in the chain throws an exception. (as long as this exception is
 * forwarded all the way through).
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface SessionProvider
{
    // ===========================================================================
    /**
     * Returns the session to be used.
     *
     * @return The session to use.
     */
    <T extends TransactionalSession> T getSession();

    // ===========================================================================
    /**
     * Returns the session to be used and requests the callback specified to be
     * invoked when the session is committed or rolled back.
     *
     * @param callback The callback to notify.
     * @return The session to use.
     */
    <T extends TransactionalSession> T getSession(TransactionCallback callback);

    // ===========================================================================
    /**
     * Commits the current session. This method will only do something if, the framework
     * is not running in enhanced mode, and the issuer is the last method in the stack.
     */
    void commitCurrentSession();

    // ===========================================================================
    /**
     * Rolls back the current session. This method will only do something if, the framework
     * is not running in enhanced mode, and the issuer is the last method in the stack.
     */
    void rollbackCurrentSession();
}
