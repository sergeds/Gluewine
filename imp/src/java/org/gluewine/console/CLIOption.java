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
