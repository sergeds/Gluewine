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
     * @param userid The user the session is for.
     * @return The new session.
     */
    String createNewSession(String userid);

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

    // ===========================================================================
    /**
     * Registers the given session as the session for the current thread.
     *
     * @param session The session id.
     */
    void setCurrentSessionId(String session);

    // ===========================================================================
    /**
     * Clears the session for the current thread.
     */
    void clearCurrentSessionId();

    // ===========================================================================
    /**
     * Returns the current session, if any.
     * @return The current session.
     */
    String getCurrentSessionId();
}
