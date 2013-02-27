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
package org.gluewine.console.impl;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.glue.Gluer;
import org.gluewine.launcher.Launcher;

/**
 * CommandProvider providing some system commands.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SystemCommandProvider implements CommandProvider
{
    // ===========================================================================
    /**
     * The gluer instance.
     */
    @Glue
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_NULL_FIELD")
    private Gluer gluer = null;

    // ===========================================================================
    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();
        m.put("ls", "Lists all jarfiles that have been loaded.");
        m.put("services", "Lists all active services");
        m.put("shutdown", "Shuts the framework down.");
        m.put("unresolved", "Lists all unresolved services.");
        return m;
    }

    // ===========================================================================
    /**
     * Executes the ls command.
     *
     * @param ci The current context.
     */
    public void _ls(CommandContext ci)
    {
        for (File file : Launcher.getInstance().getJarFiles())
            ci.println(file.getName());
    }

    // ===========================================================================
    /**
     * Executes the services command.
     *
     * @param ci The current context.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_UNWRITTEN_FIELD")
    public void _services(CommandContext ci)
    {
        Set<String> names = new TreeSet<String>();
        for (Object o : gluer.getActiveServices())
            names.add(o.getClass().getName());

        for (String name : names)
            ci.println(name);
    }

    // ===========================================================================
    /**
     * Executes the shutdown command.
     *
     * @param ci The shutdown command.
     */
    public void _shutdown(CommandContext ci)
    {
        gluer.shutdown();
    }

    // ===========================================================================
    /**
     * Executes the unresolved command.
     *
     * @param ci The current context.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_UNWRITTEN_FIELD")
    public void _unresolved(CommandContext ci)
    {
        List<Object> active = gluer.getActiveServices();
        List<Object> services = gluer.getDefinedServices();
        Set<String> names = new TreeSet<String>();
        for (Object o : services)
        {
            if (!active.contains(o))
                names.add(o.getClass().getName());
        }

        for (String name : names)
            ci.println(name);
    }
}
