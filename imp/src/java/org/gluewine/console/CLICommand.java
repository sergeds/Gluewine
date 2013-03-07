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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Defines a command that can be entered through the console.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CLICommand
{
    // ===========================================================================
    /**
     * The name of the command.
     * <br>(without leading underscore '_')
     */
    private String name = null;

    /**
     * The description of the command.
     */
    private String description = null;

    /**
     * Set of required options.
     */
    private Set<CLIOption> options = new HashSet<CLIOption>();

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param name The name of the command.
     * @param description The command description.
     */
    public CLICommand(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    // ===========================================================================
    /**
     * Returns the name of the command.
     *
     * @return The name of the command.
     */
    public String getName()
    {
        return name;
    }

    // ===========================================================================
    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return description;
    }

    // ===========================================================================
    /**
     * Adds an option.
     *
     * @param option The option to add.
     */
    public void addOption(CLIOption option)
    {
        options.add(option);
    }

    // ===========================================================================
    /**
     * Returns the (sorted) set of options. If the command has no options, an
     * empty set is returned.
     *
     * @return The set of options.
     */
    public Set<CLIOption> getOptions()
    {
        Set<CLIOption> s = new TreeSet<CLIOption>();
        s.addAll(options);
        return s;
    }
}
