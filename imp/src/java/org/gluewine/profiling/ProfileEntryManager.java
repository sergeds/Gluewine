package org.gluewine.profiling;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.core.glue.Gluer;
import org.gluewine.core.glue.Service;
import org.gluewine.persistence.SessionProvider;
import org.gluewine.persistence.Transactional;

/**
 * Manages the profile entries.
 * Remark that this service cannot be profiled, as this would result in an endless loop.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ProfileEntryManager implements CommandProvider, Runnable
{
    // ===========================================================================
    /**
     * The session provider to use.
     */
    @Glue
    private SessionProvider provider;

    /**
     * The profiler to update.
     */
    private Profiler profiler;

    /**
     * The gluer, needed to obtain the list of active services.
     */
    @Glue
    private Gluer gluer;

    /**
     * The repository where to register the profiler.
     */
    @Glue
    private Repository repository = null;

    /**
     * Flag indicating that the thread should be stopped.
     */
    private boolean stopRequested = false;

    /**
     * The list of entries to be saved.
     */
    private List<ProfileEntry> entries = new Vector<ProfileEntry>();

    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance.
     */
    public ProfileEntryManager()
    {
        profiler = new Profiler(this);
    }

    // ===========================================================================
    /**
     * Launches the thread.
     */
    @RunOnActivate
    public void launch()
    {
        stopRequested = false;
        Thread th = new Thread(this, "ProfilingThread");
        th.setDaemon(false);
        th.setPriority(Thread.MIN_PRIORITY);
        th.start();
    }

    // ===========================================================================
    /**
     * Terminates the thread.
     */
    @RunOnDeactivate
    public void terminate()
    {
        stopRequested = true;
    }

    // ===========================================================================
    /**
     * Queues an entry to be saved.
     *
     * @param entry The entry to add.
     */
    void queueEntry(ProfileEntry entry)
    {
        entries.add(entry);
    }

    // ===========================================================================
    /**
     * Adds the given entry.
     *
     * @param entry The entry to add.
     */
    @Transactional
    public void addEntry(ProfileEntry entry)
    {
        provider.getSession().add(entry);
    }

    // ===========================================================================
    /**
     * Executes the profiler_list command.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _profiler_list(CommandContext cc) throws Throwable
    {
        cc.tableHeader("ServiceName", "Method", "Profiling");
        List<Service> services = gluer.getServices();

        boolean added = false;
        for (Service s : services)
        {
            if (s.isEnhanced())
            {
                if (added)
                {
                    cc.tableSeparator();
                    added = false;
                }
                Object o = s.getActualService();
                Set<String> methods = profiler.getEntries(o);

                Class<?> cl = o.getClass().getSuperclass();
                String name = cl.getName();

                for (Method m : cl.getDeclaredMethods())
                {
                    if (Modifier.isPublic(m.getModifiers()))
                    {
                        String profiled = "";
                        if (methods != null && methods.contains(m.getName())) profiled = "*";
                        cc.tableRow(name, m.getName(), profiled);
                        name = "";
                        added = true;
                    }
                }
            }
        }

        cc.printTable();
    }

    // ===========================================================================
    /**
     * Executes the profiler_start command.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _profiler_start(CommandContext cc) throws Throwable
    {
        List<Service> services = gluer.getServices();

        String clazz = cc.getOption("-class");
        String meth = cc.getOption("-method");
        for (Service s : services)
        {
            if (s.isEnhanced())
            {
                Object o = s.getActualService();
                if (o != this && o != profiler)
                {
                    Class<?> cl = o.getClass().getSuperclass();
                    if (clazz.equals("*") || cl.getName().startsWith(clazz))
                    {
                        for (Method m : o.getClass().getDeclaredMethods())
                        {
                            if (Modifier.isPublic(m.getModifiers()))
                            {
                                if (meth.equals("*") || m.getName().startsWith(meth))
                                    profiler.startProfiling(o, m.getName());
                            }
                        }
                    }
                }
            }
        }

        if (profiler.hasProfilingEntries()) repository.register(profiler);
    }

    // ===========================================================================
    /**
     * Executes the profiler_stop command.
     *
     * @param cc The current context.
     * @throws Throwable If an error occurs.
     */
    public void _profiler_stop(CommandContext cc) throws Throwable
    {
        List<Service> services = gluer.getServices();

        String clazz = cc.getOption("-class");
        String meth = cc.getOption("-method");
        for (Service s : services)
        {
            if (s.isEnhanced())
            {
                Object o = s.getActualService();
                if (clazz.equals("*") || o.getClass().getName().startsWith(clazz))
                {
                    for (Method m : o.getClass().getDeclaredMethods())
                    {
                        if (Modifier.isPublic(m.getModifiers()))
                        {
                            if (meth.equals("*") || m.getName().startsWith(meth))
                                profiler.stopProfiling(o, m.getName());
                        }
                    }
                }
            }
        }
        if (!profiler.hasProfilingEntries()) repository.unregister(profiler);
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<CLICommand>();

        l.add(new CLICommand("profiler_list", "Lists the available services, and the methods"));

        CLICommand cmd = new CLICommand("profiler_start", "Starts profiling a class. Use partial names or * to profile 1 or more entries");
        cmd.addOption(new CLIOption("-class", "The (full, partial or *) classname", true, true));
        cmd.addOption(new CLIOption("-method", "The (full, partial or *) method name", true, true));
        l.add(cmd);

        cmd = new CLICommand("profiler_stop", "Stops profiling a class. Use partial names or * to stop 1 or more entries");
        cmd.addOption(new CLIOption("-class", "The (full, partial or *) classname", true, true));
        cmd.addOption(new CLIOption("-method", "The (full, partial or *) method name", true, true));
        l.add(cmd);

        return l;
    }

    // ===========================================================================
    @Override
    public void run()
    {
        while (!stopRequested)
        {
            try
            {
                while (entries.isEmpty() && !stopRequested)
                    Thread.sleep(500);

                while (!entries.isEmpty())
                {
                    ProfileEntry e = entries.remove(0);
                    addEntry(e);
                }
            }
            catch (InterruptedException e)
            {
                break;
            }
            catch (Throwable e)
            {
                logger.warn(e);
            }
        }
    }
}
