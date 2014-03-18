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
package org.gluewine.console.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;

/**
 * Interpreter that can be used for JUnit testing CommandProviders.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class TestCommandInterpreter
{
    // ===========================================================================
    /**
     * The provider being tested.
     */
    private CommandProvider provider = null;

    /**
     * The map of available commands.
     */
    private Map<String, CLICommand> commands = new TreeMap<String, CLICommand>();

    /**
     * The current interpreter.
     */
    private BufferedCommandInterpreter interpreter = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param provider The provider to test.
     */
    public TestCommandInterpreter(CommandProvider provider)
    {
        this.provider = provider;
        for (CLICommand cmd : provider.getCommands())
            commands.put(cmd.getName(), cmd);
    }

    // ===========================================================================
    /**
     * Executes a command.
     *
     * @param command The command to execute.
     */
    public void exec(String command)
    {
        interpreter = null;
        int i = command.indexOf(' ');
        String params = null;

        if (i > 0)
        {
            params = command.substring(i).trim();
            command = command.substring(0, i);
        }

        CLICommand cmd = commands.get(command);
        if (cmd != null)
        {
            if (cmd.isAlias())
            {
                String alias = cmd.getAlias();
                i = command.indexOf(' ');
                if (i > 0)
                {
                    params = params + alias.substring(i);
                    command = alias.substring(0, i);
                }
                else command = alias;
            }

            try
            {
                interpreter = new BufferedCommandInterpreter(params);
                Set<CLIOption> options = cmd.getOptions();
                if (!options.isEmpty())
                {
                    Map<String, boolean[]> opts = new HashMap<String, boolean[]>();
                    for (CLIOption opt : options)
                    {
                        boolean[] bool = new boolean[] {opt.isRequired(), opt.needsValue()};
                        opts.put(opt.getName(), bool);
                    }

                    interpreter.parseOptions(opts, "");
                }

                Method m = provider.getClass().getMethod("_" + command, CommandContext.class);
                m.invoke(provider, new Object[] {interpreter});
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }
        else throw new RuntimeException("Invalid command");
    }

    // ===========================================================================
    /**
     * Returns true if there's output available.
     *
     * @return True if output available.
     */
    public boolean hasOutput()
    {
        boolean hasOutput = false;
        if (interpreter != null)
        {
            String output = interpreter.getOutput();
            hasOutput = output != null && output.length() > 0;
        }

        return hasOutput;
    }
}
