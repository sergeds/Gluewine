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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.GluewineProperties;
import org.gluewine.core.glue.Gluer;
import org.gluewine.core.glue.Service;
import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.GluewineLoader;
import org.gluewine.launcher.Launcher;
import org.gluewine.launcher.SourceVersion;
import org.gluewine.launcher.sources.MissingCodeSource;

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
        update.addOption(new CLIOption("-v", "Show reloads", false, true));
        update.addOption(new CLIOption("-i", "Install Missing", false, false));
        commands.add(update);

        CLICommand reload = new CLICommand("reload", "Displays or reloads the codesources that have changed.");
        reload.addOption(new CLIOption("-f", "Does the reload", false, false));
        commands.add(reload);

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
        commands.add(new CLICommand("close", "Closes the console client."));
        commands.add(new CLICommand("cls", "Clears the screen."));
        commands.add(new CLICommand("clear", "Clears the screen."));
        commands.add(new CLICommand(">", "Routes the output to a file."));
        commands.add(new CLICommand(">!", "Stops routing the output to a file."));
        commands.add(new CLICommand("exit", "Closes the console client."));
        commands.add(new CLICommand("logoff", "Logs off the console client."));
        commands.add(new CLICommand("props_list", "Lists the property files in use."));

        CLICommand dump = new CLICommand("dump", "Dumps the class usage.");
        dump.addOption(new CLIOption("-p", "Only dumps packages", false, false));
        dump.addOption(new CLIOption("-r", "Only dumps references", false, false));
        commands.add(dump);

        CLICommand refresh = new CLICommand("props_refresh", "Refreshes the property file specified.");
        refresh.addOption(new CLIOption("-cfg", "The config file", true, true));
        commands.add(refresh);

        return commands;
    }

    // ===========================================================================
    /**
     * Executes the props_list command.
     *
     * @param cc The current context.
     */
    public void _props_list(CommandContext cc)
    {
        Map<String, GluewineProperties> m = GluewineProperties.getActiveProperties();

        cc.tableHeader("Property File", "In Use By", "Refresh Method");
        String lastUser = null;
        for (Entry<String, GluewineProperties> e : m.entrySet())
        {
            String ref = e.getValue().getRefreshMethodName();
            if (ref == null) ref = "";

            String name = e.getKey();
            if (lastUser != null && name.equals(lastUser))
                name = "";

            cc.tableRow(name, e.getValue().getOwnerClassName(), ref);

            lastUser = e.getKey();
        }

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Executes the props_list command.
     *
     * @param cc The current context.
     * @throws Throwable If the load fails.
     */
    public void _props_refresh(CommandContext cc) throws Throwable
    {
        Map<String, GluewineProperties> m = GluewineProperties.getActiveProperties();
        GluewineProperties prop = m.get(cc.getOption("-cfg"));

        if (prop != null) prop.refresh();
        else cc.println("There is no config " + cc.getOption("-cfg") + " or it is not refreshable.");
    }

    // ===========================================================================
    /**
     * Executes the ls command.
     *
     * @param ci The current context.
     */
    public void _ls(CommandContext ci)
    {
        ci.tableHeader("Name", "Type", "Ver", "Rev", "RepRev", "Build", "Checksum");
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
                }
            }
        }
        else cc.println("You must specify a service name!");
    }

    // ===========================================================================
    /**
     * Executes the dump command.
     *
     * @param cc The current context.
     */
    public void _dump(CommandContext cc)
    {
        if (cc.hasOption("-p")) dumpPackages(cc);
        else if (cc.hasOption("-r")) dumpReferences(cc);
        else dumpClasses(cc);
    }

    // ===========================================================================
    /**
     * Dumps the references.
     *
     * @param cc The current context.
     */
    private void dumpReferences(CommandContext cc)
    {
        cc.tableHeader("CodeSource", "Referenced loader");
        HashSet<GluewineLoader> loaders = new HashSet<GluewineLoader>();
        Iterator<CodeSource> sIter = Launcher.getInstance().getSources().iterator();
        while (sIter.hasNext())
        {
            CodeSource source = sIter.next();
            GluewineLoader loader = source.getSourceClassLoader();
            if (!loaders.contains(loader))
            {
                String name = source.getDisplayName();
                Iterator<String> iter = loader.getReferences().iterator();
                while (iter.hasNext())
                {
                    cc.tableRow(name, iter.next());
                    name = "";
                }

                if (!name.equals("")) cc.tableRow(name, "");
                if (sIter.hasNext()) cc.tableSeparator();
            }

            loaders.add(loader);
        }

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Dumps the packages information.
     *
     * @param cc The current context.
     */
    private void dumpPackages(CommandContext cc)
    {
        cc.tableHeader("CodeSource", "Internal Classes", "External Classes");
        HashSet<GluewineLoader> loaders = new HashSet<GluewineLoader>();
        Iterator<CodeSource> sIter = Launcher.getInstance().getSources().iterator();
        while (sIter.hasNext())
        {
            CodeSource source = sIter.next();
            GluewineLoader loader = source.getSourceClassLoader();
            if (!loaders.contains(loader))
            {
                String name = source.getDisplayName();
                Set<String> intPackages = new TreeSet<String>();
                Set<String> extPackages = new TreeSet<String>();
                for (String s : loader.getInternalClasses())
                {
                    int i = s.lastIndexOf('.');
                    if (i > 0)
                    {
                        s = s.substring(0, i);
                        intPackages.add(s);
                    }
                }

                for (String s : loader.getExternalClasses())
                {
                    int i = s.lastIndexOf('.');
                    if (i > 0)
                    {
                        s = s.substring(0, i);
                        extPackages.add(s);
                    }
                }
                Iterator<String> intIter = intPackages.iterator();
                Iterator<String> extIter = extPackages.iterator();
                while (intIter.hasNext() || extIter.hasNext())
                {
                    String ic = "";
                    String ec = "";
                    if (intIter.hasNext()) ic = intIter.next();
                    if (extIter.hasNext()) ec = extIter.next();
                    cc.tableRow(name, ic, ec);
                    name = "";
                }

                if (!name.equals("")) cc.tableRow(name, "", "");
                if (sIter.hasNext()) cc.tableSeparator();
            }
            loaders.add(loader);
        }

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Dumps the classes information.
     *
     * @param cc The current context.
     */
    private void dumpClasses(CommandContext cc)
    {
        cc.tableHeader("CodeSource", "Internal Classes", "External Classes");
        HashSet<GluewineLoader> loaders = new HashSet<GluewineLoader>();
        Iterator<CodeSource> sIter = Launcher.getInstance().getSources().iterator();
        while (sIter.hasNext())
        {
            CodeSource source = sIter.next();
            GluewineLoader loader = source.getSourceClassLoader();
            if (!loaders.contains(loader))
            {
                String name = source.getDisplayName();
                Iterator<String> intIter = loader.getInternalClasses().iterator();
                Iterator<String> extIter = loader.getExternalClasses().iterator();
                while (intIter.hasNext() || extIter.hasNext())
                {
                    String ic = "";
                    String ec = "";
                    if (intIter.hasNext()) ic = intIter.next();
                    if (extIter.hasNext()) ec = extIter.next();
                    cc.tableRow(name, ic, ec);
                    name = "";
                }

                if (!name.equals("")) cc.tableRow(name, "", "");
                if (sIter.hasNext()) cc.tableSeparator();
            }

            loaders.add(loader);
        }

        cc.printTable();
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
        gluer.stop(getIds(ci), true);
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
        gluer.unglue(getIds(ci), true);
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
        gluer.unresolve(getIds(ci), true);
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
     * Executes the reload command.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _reload(CommandContext cc) throws Throwable
    {
        if (cc.hasOption("-f")) Launcher.getInstance().reload();

        else
        {
            List<CodeSource> sources = Launcher.getInstance().getChangedSources();
            if (sources.isEmpty())
                cc.println("Nothing to reload.");

            else
            {
                List<CodeSource> toReload = new ArrayList<CodeSource>();
                Launcher.getInstance().getSourcesToReload(sources, toReload);

                cc.println("Changes sources:");
                cc.println();
                for (CodeSource src : sources)
                    cc.println(src.getDisplayName());

                cc.println("");
                cc.println("Sources that will reload:");
                cc.println("");
                for (CodeSource src : toReload)
                    cc.println('\t' + src.getDisplayName());
            }
        }
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

        Launcher.getInstance().remove(toRemove);
    }

    // ===========================================================================
    /**
     * Executes the install command.
     *
     * @param ci The current context.
     * @throws Throwable Thrown if a problem occurs.
     */
    public void _install(CommandContext ci) throws Throwable
    {
        String jar = ci.nextArgument();
        List<SourceVersion> toAdd = new ArrayList<SourceVersion>();

        while (jar != null)
        {
            String dsp = jar;
            int i = dsp.lastIndexOf('/');
            if (i > 0 && i < dsp.length() - 1) dsp = dsp.substring(i + 1);
            MissingCodeSource cs = new MissingCodeSource();
            cs.setDisplayName(dsp);
            SourceVersion sv = new SourceVersion(cs, "0", "0", jar);
            toAdd.add(sv);
            jar = ci.nextArgument();
        }

        Launcher.getInstance().add(toAdd);
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

        List<SourceVersion> updates = Launcher.getInstance().getCodeSourceToUpdate(ci.hasOption("-i"));
        String source = Launcher.getInstance().getSourceRepositoryURL();

        List<CodeSource> toRemove = new ArrayList<CodeSource>();
        for (SourceVersion sv : updates)
        {
            if (!(sv.getSource() instanceof MissingCodeSource))
                toRemove.add(sv.getSource());
        }
        if (ci.hasOption("-f"))
        {

            Launcher.getInstance().removeSources(toRemove, false);
            Launcher.getInstance().add(updates);
        }
        else
        {
            ci.println("Sources that will be updated:");
            ci.tableHeader("CodeSource", "URL");
            for (SourceVersion sv : updates)
            {
                String name = sv.getSource().getDisplayName().substring(1);
                ci.tableRow(name, source + name);
            }

            ci.printTable();

            if (ci.hasOption("-v"))
            {
                List<CodeSource> toReload = new ArrayList<CodeSource>();
                Launcher.getInstance().getSourcesToReload(toRemove, toReload);
                ci.println();
                ci.println("Sources that will be reloaded:");

                for (CodeSource src : toReload)
                    ci.println('\t' + src.getDisplayName());
            }
        }
    }
}
