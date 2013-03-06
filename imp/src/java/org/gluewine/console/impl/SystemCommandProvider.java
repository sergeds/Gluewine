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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.CodeSourceListener;
import org.gluewine.core.Glue;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.glue.Gluer;
import org.gluewine.core.glue.Service;
import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.GluewineClassLoader;
import org.gluewine.launcher.Launcher;
import org.gluewine.launcher.loaders.DirectoryJarClassLoader;

/**
 * CommandProvider providing some system commands.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SystemCommandProvider implements CommandProvider, RepositoryListener<CodeSourceListener>
{
    // ===========================================================================
    /**
     * The gluer instance.
     */
    @Glue
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_NULL_FIELD")
    private Gluer gluer = null;

    /**
     * The set of registered jar listeners.
     */
    private Set<CodeSourceListener> listeners = new HashSet<CodeSourceListener>();

    /**
     * Compares services based on the service name.
     */
    private static class ServiceNameComparator implements Comparator<Service>
    {
        @Override
        public int compare(Service o1, Service o2)
        {
            return o1.getActualService().getClass().getName().compareTo(o2.getActualService().getClass().getName());
        }
    }

    /**
     * Compares services based on the service id.
     */
    private static class ServiceIdComparator implements Comparator<Service>
    {
        @Override
        public int compare(Service o1, Service o2)
        {
            int i1 = o1.getId();
            int i2 = o2.getId();

            if (i1 < i2) return -1;
            else if (i1 > i2) return 1;
            else return 0;
        }
    }

    // ===========================================================================
    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();
        m.put("ls", "Lists all jarfiles that have been loaded.");
        m.put("services", "Lists all active services");
        m.put("shutdown", "Shuts the framework down.");
        m.put("unresolved", "Lists all unresolved services.");
        m.put("remove", "<jar> Removes the jar specified.");
        m.put("stop", "<service name> Stops all active services that start with the given name.");
        m.put("loaders", "<service name> Shows the classloader dispatching of the given service(s).");
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
        ci.tableHeader("Name", "Type", "Version", "Revision", "Repos. Revision", "BuildNumber", "Checksum");
        for (CodeSource source : Launcher.getInstance().getSources())
            ci.tableRow(source.getDisplayName(), source.getType(),
                        source.getVersion(), source.getRevision(), source.getReposRevision(),
                        source.getBuildNumber(), source.getChecksum());

        ci.printTable();
    }

    // ===========================================================================
    /**
     * Executes the services command.
     *
     * @param ci The current context.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_UNWRITTEN_FIELD")
    public void _services(CommandContext ci) throws Throwable
    {
        Map<String, boolean[]> opts = new HashMap<String, boolean[]>();
        opts.put("-id", new boolean[] {false, false});
        ci.parseOptions(opts, "[-id]");

        ci.tableHeader("ID", "Service", "Enhanced", "Resolved", "Glued", "Active");

        List<Service> services = gluer.getServices();

        if (ci.hasOption("-id")) Collections.sort(services, new ServiceIdComparator());
        else Collections.sort(services, new ServiceNameComparator());

        for (Service service : services)
        {
            ci.tableRow(Integer.toString(service.getId()), service.getName(),
                        Boolean.toString(service.isEnhanced()), Boolean.toString(service.isResolved()),
                        Boolean.toString(service.isGlued()), Boolean.toString(service.isActive()));
        }

        ci.printTable();
    }

    // ===========================================================================
    /**
     * Executes the loaders command.
     *
     * @param cc The current context.
     */
    public void _loaders(CommandContext cc)
    {
        String service = cc.nextArgument();
        if (service != null)
        {
            for (Service s : gluer.getServices())
            {
                if (s.getName().startsWith(service))
                {
                    ClassLoader cl = s.getActualService().getClass().getClassLoader();
                    if (s.isEnhanced())
                        cl = s.getActualService().getClass().getSuperclass().getClassLoader();

                    cc.println("Service     : " + s.getName());
                    cc.println("Base Loader : " + cl.toString());

                    if (cl instanceof GluewineClassLoader)
                        displayDispatchers((GluewineClassLoader) cl, "\t", new HashSet<GluewineClassLoader>(), cc);
                }
            }
        }
        else cc.println("You must specify a service name!");
    }

    // ===========================================================================
    /**
     * Displays the dispatching chaing of the given classloader.
     *
     * @param cl The classloader to display.
     * @param prefix The prefix to use for indentation.
     * @param loaders The set of loaders already displayed, to avoid infinite looping.
     * @param cc The current context.
     */
    private void displayDispatchers(GluewineClassLoader cl, String prefix, Set<GluewineClassLoader> loaders, CommandContext cc)
    {
        GluewineClassLoader gwl = (GluewineClassLoader) cl;
        loaders.add(gwl);

        cc.println(prefix + "Dispatches to : ");
        for (GluewineClassLoader disp : gwl.getAllDispatchers())
        {
            if (!loaders.contains(disp))
            {
                loaders.add(disp);
                cc.println(prefix + "\t" + disp.toString());
                if (disp instanceof DirectoryJarClassLoader)
                    displayDispatchers(disp, prefix + "\t", loaders, cc);
            }
        }
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
    public void _stop(CommandContext ci) throws Throwable
    {
        gluer.stop(getIds(ci));
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
    public void _unglue(CommandContext ci) throws Throwable
    {
        gluer.unglue(getIds(ci));
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
    public void _unresolve(CommandContext ci) throws Throwable
    {
        gluer.unresolve(getIds(ci));
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
    public void _resolve(CommandContext ci) throws Throwable
    {
        gluer.resolve(getIds(ci));
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
    public void _glue(CommandContext ci) throws Throwable
    {
        gluer.glue(getIds(ci));
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
    public void _start(CommandContext ci) throws Throwable
    {
        gluer.start(getIds(ci));
    }

    // ===========================================================================
    /**
     * Returns the array of ids entered in the console.
     *
     * @param ci The current context.
     * @return The array of ids.
     */
    private int[] getIds(CommandContext ci)
    {
        int[] ids = new int[ci.getArgumentCount()];

        for (int i = 0; i < ids.length; i++)
        {
            String n = ci.nextArgument();
            if (n != null && n.trim().length() > 0)
                ids[i] = Integer.valueOf(n);
        }

        return ids;
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

    // ===========================================================================
    /**
     * Executes the remove command.
     *
     * @param ci The current context.
     */
    public void _remove(CommandContext ci)
    {
        String jar = ci.nextArgument();
        List<String> toRemove = new ArrayList<String>();

        while (jar != null)
        {
            toRemove.add(jar);
            jar = ci.nextArgument();
        }

        List<CodeSource> removed = Launcher.getInstance().remove(toRemove);
        for (CodeSourceListener jl : listeners)
            jl.codeSourceRemoved(removed);
    }

    // ===========================================================================
    /**
     * Executes the install command.
     *
     * @param ci The current context.
     */
    public void _install(CommandContext ci)
    {
        String jar = ci.nextArgument();
        List<String> toAdd = new ArrayList<String>();

        while (jar != null)
        {
            toAdd.add(jar);
            jar = ci.nextArgument();
        }

        List<CodeSource> added = Launcher.getInstance().add(toAdd);
        for (CodeSourceListener jl : listeners)
            jl.codeSourceAdded(added);
    }

    // ===========================================================================
    @Override
    public void registered(CodeSourceListener t)
    {
        listeners.add(t);
    }

    // ===========================================================================
    @Override
    public void unregistered(CodeSourceListener t)
    {
        listeners.remove(t);
    }
}
