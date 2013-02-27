/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence;

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
     * @throws PersistenceException Thrown if the session could not be initialized.
     */
    TransactionalSession getSession() throws PersistenceException;

    // ===========================================================================
    /**
     * Returns the session to be used and requests the callback specified to be
     * invoked when the session is committed or rolled back.
     *
     * @param callback The callback to notify.
     * @return The session to use.
     * @throws PersistenceException Thrown if the session could not be initialized.
     */
    TransactionalSession getSession(TransactionCallback callback) throws PersistenceException;

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
