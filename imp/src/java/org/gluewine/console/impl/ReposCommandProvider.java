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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.glue.RepositoryImpl;

/**
 * CommandProvider that interacts with the repository.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ReposCommandProvider implements CommandProvider
{
    // ===========================================================================
    /**
     * The object repository.
     */
    @Glue
    private RepositoryImpl repos = null;

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();
        commands.add(new CLICommand("repos_info", "Shows the number of registered objects and listeners in the repository."));
        commands.add(new CLICommand("repos_objects", "Lists the registered objects."));
        commands.add(new CLICommand("repos_listeners", "Lists the registered listeners."));
        return commands;
    }

    // ===========================================================================
    /**
     * Displays the list of registered objects.
     *
     * @param cc The list of registered objects.
     * @throws Throwable If a problem occurs.
     */
    public void _repos_objects(CommandContext cc) throws Throwable
    {
        cc.tableHeader("Object");
        for (String s : repos.getRegisteredObjects())
            cc.tableRow(s);
        cc.printTable();
    }

    // ===========================================================================
    /**
     * Displays the list of registered listeners.
     *
     * @param cc The list of registered listeners.
     * @throws Throwable If a problem occurs.
     */
    public void _repos_listeners(CommandContext cc) throws Throwable
    {
        cc.tableHeader("Listener");
        Set<String> sl = new TreeSet<String>();
        for (RepositoryListener<?> l : repos.getRegisteredListeners())
            sl.add(l.toString());

        for (String s : sl)
            cc.tableRow(s);

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Shows the count of registered objects and listeners.
     *
     * @param cc The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _repos_info(CommandContext cc) throws Throwable
    {
        cc.tableHeader("Type", "Amount");
        int o = repos.getRegisteredObjectCount();
        int l = repos.getRegisteredListenerCount();
        cc.tableRow("Objects", Integer.toString(o));
        cc.tableRow("Listeners", Integer.toString(l));
        cc.printTable();
    }

    // ===========================================================================
    /**
     * Shows the source (jar file) of all registered objects and listeners.
     *
     * @param cc The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _repos_source(CommandContext cc) throws Throwable
    {
        cc.tableHeader("Object", "Class", "Source");
        Map<String, Object> obs = repos.getRegisteredObjectMap();
        for (Entry<String, Object> e : obs.entrySet())
        {
            Class<?> clazz = e.getValue().getClass();
            if (clazz.getName().indexOf("$$Enhancer") > 0) clazz = clazz.getSuperclass();
            String source = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();

            cc.tableRow(e.getKey(), clazz.getName(), source);
        }

        cc.printTable();
    }
}
