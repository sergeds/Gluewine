/**************************************************************************
 *
 * Gluewine System Management Module
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
package org.gluewine.sysmgm;

import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;

/**
 * System Management Command Provider.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SysmgmCommandProvider implements CommandProvider
{
    // ===========================================================================
    /**
     * Compares 2 thread based on their CPU usage.
     */
    private static class UsageComparator implements Comparator<ThreadInfo>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = 3344904751140689374L;

        /**
         * The bean to use.
         */
        private ThreadMXBean tbean = null;

        /**
         * Creates an instance.
         *
         * @param bean The bean.
         */
        public UsageComparator(ThreadMXBean bean)
        {
            tbean = bean;
        }

        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2)
        {
            long c1 = tbean.getThreadCpuTime(o1.getThreadId());
            long c2 = tbean.getThreadCpuTime(o2.getThreadId());
            if (c1 < c2)
                return -1;
            else if (c1 > c2)
                return 1;
            else
                return 0;
        }
    };

    // ===========================================================================
    /**
     * Comparator that uses the thread name to sort the threads.
     *
     */
    private static class NameComparator implements Comparator<ThreadInfo>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = -5969890183137255256L;

        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2)
        {
            return o1.getThreadState().name().compareTo(o2.getThreadState().name());
        }
    };

    // ===========================================================================
    /**
     * Comparator that uses the thread id to sort them.
     */
    private static class IdComparator implements Comparator<ThreadInfo>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = -3814612871110391878L;

        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2)
        {
            long i1 = o1.getThreadId();
            long i2 = o2.getThreadId();
            if (i1 < i2) return -1;
            else if (i1 > i2) return 1;
            else return 0;
        }
    };

    // ===========================================================================
    /**
     * Returns CPU Usage information on a per thread base.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_usage(CommandContext ci) throws Throwable
    {

        final ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos  = tbean.getThreadInfo(tbean.getAllThreadIds(), 1);

        if (ci.hasOption("-cpu"))
            Arrays.sort(infos, new UsageComparator(tbean));

        else if (ci.hasOption("-state"))
            Arrays.sort(infos, new NameComparator());

        else if (ci.hasOption("-id"))
            Arrays.sort(infos, new IdComparator());

        ci.tableHeader("Thread", "State", "CPU Time", "Blocked Time", "Class");
        for (ThreadInfo info : infos)
        {
            String clazz = null;
            if (info.getStackTrace().length > 0)
            {
                StackTraceElement el = info.getStackTrace()[0];
                clazz = el.getClassName() + "." + el.getMethodName();
            }

            ci.tableRow(info.getThreadId() + "-" + info.getThreadName(), info.getThreadState().name(),
                        Long.toString(tbean.getThreadCpuTime(info.getThreadId()) - 1000),
                        Long.toString(info.getBlockedTime()), clazz);
        }

        ci.printTable();
    }

    // ===========================================================================
    /**
     * Lists the available fonts.
     *
     * @param ci The current context.
     * @throws Throwable If an error occurs.
     */
    public void _sysmgm_fontlist(CommandContext ci) throws Throwable
    {
        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : fonts)
            ci.println(font);
    }

    // ===========================================================================
    /**
     * Displays information about how the application was started.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_info(CommandContext ci) throws Throwable
    {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        ci.println("BootClassPath: " + rt.getBootClassPath());
        ci.println("ClassPath: " + rt.getClassPath());
        StringBuilder args = new StringBuilder();
        for (String s : rt.getInputArguments())
            args.append(s).append(" ");
        ci.println("Arguments: " + args.toString());
    }

    // ===========================================================================
    /**
     * Finds deadlocks.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_deadlock(CommandContext ci) throws Throwable
    {
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        long[] ids = tbean.findDeadlockedThreads();
        if (ids != null)
        {
            for (long id : ids)
            {
                ThreadInfo thread = tbean.getThreadInfo(id, Integer.MAX_VALUE);
                Map<StackTraceElement, MonitorInfo> locks = new HashMap<StackTraceElement, MonitorInfo>();
                for (MonitorInfo info : thread.getLockedMonitors())
                    locks.put(info.getLockedStackFrame(), info);

                display(thread, ci, locks);
                ci.println();
            }
        }
        else
            ci.println("No deadlocks found.");
    }

    // ===========================================================================
    /**
     * Enables Thread contention management.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_enablecontentionmgmt(CommandContext ci) throws Throwable
    {
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        tbean.setThreadCpuTimeEnabled(true);
        tbean.setThreadContentionMonitoringEnabled(true);
    }

    // ===========================================================================
    /**
     * Disables Thread contention management.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_diablecontentionmgmt(CommandContext ci) throws Throwable
    {
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        tbean.setThreadCpuTimeEnabled(false);
        tbean.setThreadContentionMonitoringEnabled(false);
    }

    // ===========================================================================
    /**
     * Gives thread information.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_threaddump(CommandContext ci) throws Throwable
    {
       ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
       ThreadInfo[] threads = tbean.dumpAllThreads(true, true);

       for (ThreadInfo thread : threads)
       {
           Map<StackTraceElement, MonitorInfo> locks = new HashMap<StackTraceElement, MonitorInfo>();
           for (MonitorInfo info : thread.getLockedMonitors())
               locks.put(info.getLockedStackFrame(), info);

           display(thread, ci, locks);
           ci.println();
       }
    }

    // ===========================================================================
    /**
     * Displays the thread specified.
     *
     * @param thread The thread to dump.
     * @param ci The current context.
     * @param locks The map with locks.
     * @throws Throwable If a problem occurs.
     */
    private void display(ThreadInfo thread, CommandContext ci, Map<StackTraceElement, MonitorInfo> locks) throws Throwable
    {
        ci.println("Thread id: " + thread.getThreadId() + ", name: " + thread.getThreadName() + ", state: " + thread.getThreadState().name());
        StackTraceElement[] elements = thread.getStackTrace();
        for (StackTraceElement el : elements)
        {
            StringBuilder b = new StringBuilder(" \t");
            b.append(el.getClassName()).append(".").append(el.getMethodName());
            if (el.isNativeMethod())
                b.append(" (Native)");

            else if (el.getFileName() != null)
                b.append(" (").append(el.getFileName()).append(":").append(el.getLineNumber()).append(")");

            ci.println(b.toString());

            if (thread.getThreadState().equals(State.BLOCKED))
            {
                ci.println("\t\tWaiting for monitor on: " + thread.getLockName());
            }
            if (locks.containsKey(el))
            {
                MonitorInfo lock = locks.get(el);
                ci.println(" \t\tLocked: " + lock.getIdentityHashCode());
            }
        }
    }

    // ===========================================================================
    /**
     * Performs a memory analysis of the system.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _sysmgm_memory(CommandContext ci) throws Throwable
    {
        Runtime rt = Runtime.getRuntime();

        long committed = 0;
        long peak = 0;
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans)
        {
            committed += bean.getUsage().getCommitted();
            peak += bean.getPeakUsage().getCommitted();
        }

        ci.tableHeader("Free", "Current", "Max", "Committed", "Peak", "Processors");
        ci.tableRow(Long.toString(rt.freeMemory()), Long.toString(rt.totalMemory()), Long.toString(rt.maxMemory()), Long.toString(committed), Long.toString(peak), Integer.toString(rt.availableProcessors()));
        ci.printTable();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();

        CLICommand cmd = new CLICommand("sysmgm_usage", "Returns CPU Usage information on a per thread base.");
        cmd.addOption(new CLIOption("-cpu", "sort on cpu", false, false));
        cmd.addOption(new CLIOption("-state", "sort on state", false, false));
        cmd.addOption(new CLIOption("-id", "sort on id", false, false));
        commands.add(cmd);

        commands.add(new CLICommand("sysmgm_deadlock", "Finds and displays deadlocks."));
        commands.add(new CLICommand("sysmgm_disablecontentionmgmt", "Disables Thread contention management."));
        commands.add(new CLICommand("sysmgm_enablecontentionmgmt", "Enables Thread contention management."));
        commands.add(new CLICommand("sysmgm_info", "Displays information about how the application was started."));
        commands.add(new CLICommand("sysmgm_memory", "Displays information about the memory usage."));
        commands.add(new CLICommand("sysmgm_threaddump", "Dumps all running threads."));
        commands.add(new CLICommand("sysmgm_fontlist", "Lists all available fonts."));

        return commands;
    }
}
