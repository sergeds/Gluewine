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

/**
 * Defines a command option.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CLIOption implements Comparable<CLIOption>
{
    // ===========================================================================
    /**
     * The option name.
     */
    private String name = null;

    /**
     * The description.
     */
    private String description = null;

    /**
     * Flag indicating that the option is required.
     */
    private boolean required = true;

    /**
     * Flag indicating that the option needs a value.
     */
    private boolean value = true;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param name The name of the option.
     * @param description The description of the option.
     * @param required Whether the option is required or optional.
     * @param needsValue Whether the option needs a value.
     */
    public CLIOption(String name, String description, boolean required, boolean needsValue)
    {
        this.name = name;
        this.description = description;
        this.required = required;
        this.value = needsValue;
    }

    // ===========================================================================
    /**
     * Returns the name of the option.
     *
     * @return The name of the option.
     */
    public String getName()
    {
        return name;
    }

    // ===========================================================================
    /**
     * Returns the description of the option.
     *
     * @return The description of the option.
     */
    public String getDescription()
    {
        return description;
    }

    // ===========================================================================
    /**
     * Returns whether the option is required.
     *
     * @return The required flag.
     */
    public boolean isRequired()
    {
        return required;
    }

    // ===========================================================================
    /**
     * Returns whether the option needs a value.
     *
     * @return Wheter a value is required.
     */
    public boolean needsValue()
    {
        return value;
    }

    // ===========================================================================
    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }

    // ===========================================================================
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof CLIOption)
            return getName().equals(((CLIOption) o).getName());
        else return false;
    }

    // ===========================================================================
    @Override
    public int compareTo(CLIOption o)
    {
        return getName().compareTo(o.getName());
    }
}
