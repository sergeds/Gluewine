/**************************************************************************
 *
 * Gluewine Camel Integration Module
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
package org.gluewine.camel;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.impl.DefaultCamelContext;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;

/**
 * Class that starts the CamelContext.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineCamelContext extends DefaultCamelContext implements CommandProvider
{
    // ===========================================================================
    @Override
    @RunOnActivate
    public void start() throws Exception
    {
        super.start();
    }

    // ===========================================================================
    @Override
    @RunOnDeactivate
    public void stop() throws Exception
    {
        super.start();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> cmds = new ArrayList<CLICommand>();
        cmds.add(new CLICommand("camel_suspend", "Suspends the Camel framework."));
        cmds.add(new CLICommand("camel_resume", "Resumes the Camel framework."));
        return cmds;
    }

    // ===========================================================================
    /**
     * Executes the camel_resume command.
     *
     * @param cc The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _camel_resume(CommandContext cc) throws Throwable
    {
        super.resume();
    }

    // ===========================================================================
    /**
     * Executes the camel_suspend command.
     *
     * @param cc The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _camel_suspend(CommandContext cc) throws Throwable
    {
        super.suspend();
    }
}
