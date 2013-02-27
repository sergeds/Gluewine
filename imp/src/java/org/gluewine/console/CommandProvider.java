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

import java.util.Map;

/**
 * Defines a CommandProvider.
 *
 * <p>A CommandProvider needs to provide void methods starting with a _ (underscore) and
 * accepting 1 parameter of CommandContext.
 *
 * <p>These commands are made availabel through the console. (without the _).
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface CommandProvider
{
    // ===========================================================================
    /**
     * Returns the map of commands and their syntax.
     * The command name is the key in the map, the value is the command syntax.
     *
     * @return The Map of commands.
     */
    Map<String, String> getCommandsSyntax();
}
