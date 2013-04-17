/**************************************************************************
 *
 * Gluewine Camel Integration Module
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
