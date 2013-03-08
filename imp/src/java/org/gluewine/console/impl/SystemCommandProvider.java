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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
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
import org.gluewine.launcher.SourceVersion;
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
    private static class ServiceNameComparator implements Comparator<Service>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = -6812851888112956397L;

        @Override
        public int compare(Service o1, Service o2)
        {
            return o1.getActualService().getClass().getName().compareTo(o2.getActualService().getClass().getName());
        }
    }

    /**
     * Compares services based on the service id.
     */
    private static class ServiceIdComparator implements Comparator<Service>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = -3139019409498844549L;

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
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();
        commands.add(new CLICommand("ls", "Lists all jarfiles that have been loaded."));

        CLICommand services = new CLICommand("services", "Lists all active services.");
        services.addOption(new CLIOption("-id", "Sorts the id", false, false));
        commands.add(services);

        CLICommand update = new CLICommand("update", "Displays or updates the codesources that changed");
        update.addOption(new CLIOption("-f", "Does the update", false, false));
        update.addOption(new CLIOption("-s", "The source URL", false, true));
        commands.add(update);

        commands.add(new CLICommand("shutdown", "Shuts the framework down."));
        commands.add(new CLICommand("unresolved", "Lists all unresolved services."));
        commands.add(new CLICommand("remove", "Removes the jar specified."));
        commands.add(new CLICommand("stop", "Stops the service(s) with the specified id(s)."));
        commands.add(new CLICommand("glue", "Glues the service(s) with the specified id(s)."));
        commands.add(new CLICommand("unglue", "Unglues the service(s) with the specified id(s)."));
        commands.add(new CLICommand("start", "Starts the service(s) with the specified id(s)."));
        commands.add(new CLICommand("loaders", "Shows the classloader dispatching of the given service(s)."));
        commands.add(new CLICommand("install", "Installs the code source with the given url."));
        commands.add(new CLICommand("resolve", "Resolves the service(s) with the specified id(s)."));
        commands.add(new CLICommand("unresolve", "Unresolves the service(s) with the specified id(s)."));

        return commands;
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
    public void _services(CommandContext ci)
    {
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
    public void _stop(CommandContext ci)
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
    public void _unglue(CommandContext ci)
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
    public void _unresolve(CommandContext ci)
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
    public void _resolve(CommandContext ci)
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
    public void _glue(CommandContext ci)
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
    public void _start(CommandContext ci)
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
                int cgli = e.getKey().indexOf("$$Enhancer");
                int dscli = e.getKey().indexOf("Enhanced");
                boolean enhanced = dscli > 0;
                enhanced |= cgli > 0;

                String name = e.getKey();
                if (enhanced)
                {
                    if (dscli > 0) name = name.substring(0, dscli);
                    else if (cgli > 0) name = name.substring(0, cgli);
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
    /**
     * Executes the update command.
     *
     * @param ci The current context.
     * @throws Throwable If an error occurs.
     */
    public void _update(CommandContext ci) throws Throwable
    {
        if (ci.hasOption("-s"))
            Launcher.getInstance().setSourceRepositoryURL(ci.getOption("-s"));

        List<SourceVersion> updates = Launcher.getInstance().getCodeSourceToUpdate();
        String source = Launcher.getInstance().getSourceRepositoryURL();
        File root = Launcher.getInstance().getRoot();
        if (ci.hasOption("-f"))
        {
            for (SourceVersion sv : updates)
            {
                String name = sv.getSource().getDisplayName().substring(1);
                String url = source + name;
                ci.println("Fetching " + url);
                fetch(url, new File(root, name + ".update"), sv);
            }
        }
        else
        {
            ci.tableHeader("CodeSource", "URL");
            for (SourceVersion sv : updates)
            {
                String name = sv.getSource().getDisplayName().substring(1);
                ci.tableRow(name, source + name);
            }

            ci.printTable();
        }
    }

    // ===========================================================================
    /**
     * Fetches the bundle specified.
     *
     * @param url The url to fetch.
     * @param local The local name to save to.
     * @param source The source version to fetch.
     * @throws Throwable If an error occurs.
     */
    private void fetch(String url, File local, SourceVersion source) throws Throwable
    {
        URL conn = new URL(url);
        InputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = conn.openStream();
            out = new FileOutputStream(local);
            byte[] b = new byte[4096];
            int read = in.read(b);
            while (read > 0)
            {
                out.write(b, 0, b.length);
                read = in.read(b);
            }
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            finally
            {
                if (out != null) out.close();
            }
        }
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
