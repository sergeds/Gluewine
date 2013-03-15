/**************************************************************************
 *
 * Gluewine Console Module
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
package org.gluewine.console;

import java.util.List;
import java.util.Map;

import org.gluewine.core.RepositoryListener;

/**
 * Defines the console server.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface ConsoleServer extends RepositoryListener<CommandProvider>
{
    // ===========================================================================
    /**
     * Executes the given command and returns the output as a
     * String.
     *
     * @param command The command to execute.
     * @return The output.
     * @throws Throwable If the command failed, or the console could not
     * be contacted.
     */
    String executeCommand(String command) throws Throwable;

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
