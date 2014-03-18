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
     * An alias is the name of the method (without the leading _) that must be invoked
     * when this command is executed. (The method needs to be part of the same CommandProvider
     * that handles this command).
     * Beside the name, additional parameters can be specied. These parameters will
     * be appended with the parameters entered on the command line.
     */
    private String alias = null;

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
     * Returns the alias, if this command is an alias.
     *
     * @return The alias.
     */
    public String getAlias()
    {
        return alias;
    }

    // ===========================================================================
    /**
     * Sets the alias. When this value is set, whenever the command is invoked,
     * the alias will be used instead.
     *
     * @param alias The alias.
     */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    // ===========================================================================
    /**
     * Returns true if this command is an alias to another method.
     *
     * @return True if it is an alias.
     */
    public boolean isAlias()
    {
        return alias != null;
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
     * Adds an option. This is a convenience method and is identical to:
     * <br>addOption(new CLIOption(name, description, required, needsValue).
     *
     * @param name The name of the option.
     * @param description The description.
     * @param required True if the option is required.
     * @param needsValue True if the option requires a value.
     */
    public void addOption(String name, String description, boolean required, boolean needsValue)
    {
        options.add(new CLIOption(name, description, required, needsValue));
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
