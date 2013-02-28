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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.glue.Gluer;
import org.gluewine.core.glue.Service;
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
        m.put("stop", "Stops all active services that start with the given name.");
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
        ci.tableHeader("Service", "Enhanced", "Resolved", "Glued", "Active");
        Map<String, Service> sorted = new TreeMap<String, Service>();
        for (Service s : gluer.getServices())
        {
            sorted.put(s.getServiceClass().getCanonicalName(), s);
        }

        for (Entry<String, Service> e : sorted.entrySet())
        {
            int cgl_i = e.getKey().indexOf("$$Enhancer");
            int dscl_i = e.getKey().indexOf("Enhanced");
            boolean enhanced = dscl_i > 0;
            enhanced |= cgl_i > 0;

            String name = e.getKey();
            if (enhanced)
            {
                if (dscl_i > 0) name = name.substring(0, dscl_i);
                else if (cgl_i > 0) name = name.substring(0, cgl_i);
            }

            ci.tableRow(name, Boolean.toString(enhanced), Boolean.toString(e.getValue().isResolved()),
                        Boolean.toString(e.getValue().isGlued()), Boolean.toString(e.getValue().isActive()));
        }

        ci.printTable();
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
     * Requests the framework to stop one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be stopped.
     *
     * The stop will deregister() them from the framework and stop them.
     *
     * @param ci The current context.
     */
    public void _stop(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.stop(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
    }

    // ===========================================================================
    /**
     * Requests the framework to unglue one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be unglued.
     *
     * The unglue will stop() them from the framework and unglue them.
     *
     * @param ci The current context.
     */
    public void _unglue(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.unglue(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
    }

    // ===========================================================================
    /**
     * Requests the framework to unresolve one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be unresolved.
     *
     * The stop will unglue() them from the framework and unresolve them.
     *
     * @param ci The current context.
     */
    public void _unresolve(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.unresolve(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
    }

    // ===========================================================================
    /**
     * Requests the framework to resolve one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be resolved.
     *
     * @param ci The current context.
     */
    public void _resolve(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.resolve(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
    }

    // ===========================================================================
    /**
     * Requests the framework to glue one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be glued.
     *
     * @param ci The current context.
     */
    public void _glue(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.glue(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
    }

    // ===========================================================================
    /**
     * Requests the framework to start one or more services.
     * It requires 1 parameter:
     *
     * - a partial class name. All classes matching the given name will be started.
     *
     * @param ci The current context.
     */
    public void _start(CommandContext ci)
    {
        String name = ci.nextArgument();
        if (name != null)
        {
            gluer.start(name);
            ci.println();
            _services(ci);
        }
        else ci.println("You must specify a partial class (or package) name!");
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
        ci.tableHeader("Service", "Unresolved Fields");
        Map<String, Service> sorted = new TreeMap<String, Service>();
        for (Service s : gluer.getServices())
            sorted.put(s.getServiceClass().getCanonicalName(), s);

        for (Entry<String, Service> e : sorted.entrySet())
        {
            if (!e.getValue().isResolved())
            {
                int cgl_i = e.getKey().indexOf("$$Enhancer");
                int dscl_i = e.getKey().indexOf("Enhanced");
                boolean enhanced = dscl_i > 0;
                enhanced |= cgl_i > 0;

                String name = e.getKey();
                if (enhanced)
                {
                    if (dscl_i > 0) name = name.substring(0, dscl_i);
                    else if (cgl_i > 0) name = name.substring(0, cgl_i);
                }

                StringBuilder unres = new StringBuilder();
                for (String s : e.getValue().getUnresolvedFields())
                    unres.append(s).append(" ");
                ci.tableRow(name, unres.toString());
            }
        }

        ci.printTable();
    }
}
