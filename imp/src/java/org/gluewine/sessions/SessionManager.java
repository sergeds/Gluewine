/**************************************************************************
 *
 * Gluewine Base Session Management Module
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
package org.gluewine.sessions;

/**
 * Manages sessions.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface SessionManager
{
    // ===========================================================================
    /**
     * Creates a new session and returns its id.
     *
     * @return The new session.
     */
    String createNewSession();

    // ===========================================================================
    /**
     * Checks whether the given session is still valid. If not, a SessionExpiredException
     * is thrown.
     *
     * @param session The session to check.
     */
    void checkSession(String session);

    // ===========================================================================
    /**
     * Checks whether the given session is still valid. If not, a SessionExpiredException
     * is thrown, if the session is still valid, it is ticked.
     * <br>This is a convenience method and is exactly the same as invoking:
     * <br>checkSession(session) followed by tickSession(session).
     *
     * @param session The session to check.
     */
    void checkAndTickSession(String session);

    // ===========================================================================
    /**
     * Updates the timestamp of the given session.
     *
     * @param session The session to update.
     */
    void tickSession(String session);

    // ===========================================================================
    /**
     * Closes the given session.
     *
     * @param session The session to close.
     */
    void closeSession(String session);
}
