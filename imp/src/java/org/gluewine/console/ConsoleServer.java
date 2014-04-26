/**************************************************************************
 *
 * Gluewine Console Module
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
package org.gluewine.console;

import java.util.List;
import java.util.Map;

/**
 * Defines the console server.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface ConsoleServer
{
    // ===========================================================================
    /**
     * Executes the given command and returns the output as a
     * String.
     *
     * @param command The command to execute.
     * @return The response.
     * @throws Throwable If the command failed, or the console could not
     * be contacted.
     */
    Response executeCommand(Request command) throws Throwable;

    // ===========================================================================
    /**
     * Returns true if the server requires authentication.
     *
     * @return True if authentication required.
     */
    boolean needsAuthentication();

    // ===========================================================================
    /**
     * Retuns the map of available authenticators indexed on their user friendly
     * name.
     *
     * @return The map of authenticators.
     */
    Map<String, String> getAvailableAuthenticators();

    // ===========================================================================
    /**
     * Returns the welcome message.
     *
     * @return The welcome message.
     */
    String getWelcomeMessage();

    // ===========================================================================
    /**
     * Returns the prompt for this server.
     *
     * @return The prompt.
     */
    String getPrompt();

    // ===========================================================================
    /**
     * Completes the command specified and returns the list of options.
     *
     * @param command The command to complete.
     * @return The list of options.
     */
    List<String> complete(String command);
}
