/**************************************************************************
 *
 * Gluewine Profiler Integration Module
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
package org.gluewine.profiling;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;

/**
 * AspectProvider that allows to track performance information.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Profiler implements AspectProvider
{
    // ===========================================================================
    /**
     * The manager to use to access the database.
     */
    private ProfileEntryManager manager;

    /**
     * The map of profile entries indexed on the class/method, and thread they belong to.
     */
    private Map<Thread, Map<String, ProfileEntry>> threads = new HashMap<Thread, Map<String, ProfileEntry>>();

    /**
     * The map of objects to profile. The map contains a set of methods to profile for each
     * object. An '*' entry in the set, means that all entries are to be profiled.
     */
    private Map<Class<?>, Set<String>> toProfile = new HashMap<Class<?>, Set<String>>();

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param mgr The manager to use.
     */
    Profiler(ProfileEntryManager mgr)
    {
        this.manager = mgr;
    }

    // ===========================================================================
    /**
     * Returns true if there are entries to be profiled.
     *
     * @return True if profiling requested.
     */
    boolean hasProfilingEntries()
    {
        return !toProfile.isEmpty();
    }

    // ===========================================================================
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
        if (!toProfile.isEmpty() && toProfile.containsKey(o.getClass()))
        {
            Set<String> set = toProfile.get(o.getClass());
            if (set.contains(m.getName()))
            {
                String name = o.getClass().getName();
                int i = name.indexOf("$$EnhancerByCGLIB$$");
                if (i > -1) name = name.substring(0, i);

                Map<String, ProfileEntry> map = threads.get(Thread.currentThread());
                if (map == null)
                {
                    map = new HashMap<String, ProfileEntry>();
                    threads.put(Thread.currentThread(), map);
                }

                ProfileEntry e = new ProfileEntry(name, m.getName(), System.currentTimeMillis());
                map.put(name + "." + m.getName(), e);
            }
        }
    }

    // ===========================================================================
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        long end = System.currentTimeMillis();
        if (!toProfile.isEmpty() && toProfile.containsKey(o.getClass()))
        {
            Set<String> set = toProfile.get(o.getClass());
            if (set.contains(m.getName()))
            {
                Map<String, ProfileEntry> map = threads.get(Thread.currentThread());
                if (map != null)
                {
                    String name = o.getClass().getName();
                    int i = name.indexOf("$$EnhancerByCGLIB$$");
                    if (i > -1) name = name.substring(0, i);
                    ProfileEntry e = map.get(name + "." + m.getName());
                    if (e != null)
                    {
                        e.setDuraction(end - e.getExecutionDate());
                        e.setSuccess(true);
                        manager.queueEntry(e);
                    }
                    else
                        logger.warn("AfterSuccess invoked on profile for a registered object, but no profile entry has been set in a beforeInvocation!");
                }
                else
                    logger.warn("AfterSuccess invoked on profile for a registered object, but no profile entry has been set in a beforeInvocation!");
            }
        }
    }

    // ===========================================================================
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable t)
    {
        long end = System.currentTimeMillis();
        if (!toProfile.isEmpty() && toProfile.containsKey(o.getClass()))
        {
            Set<String> set = toProfile.get(o.getClass());
            if (set.contains(m.getName()))
            {
                Map<String, ProfileEntry> map = threads.get(Thread.currentThread());
                if (map != null)
                {
                    String name = o.getClass().getName();
                    int i = name.indexOf("$$EnhancerByCGLIB$$");
                    if (i > -1) name = name.substring(0, i);
                    ProfileEntry e = map.get(name + "." + m.getName());
                    if (e != null)
                    {
                        e.setDuraction(end - e.getExecutionDate());
                        e.setSuccess(false);
                        e.setException(t.getClass().getName());
                        manager.queueEntry(e);
                    }
                    else
                        logger.warn("AfterFailure invoked on profile for a registered object, but no profile entry has been set in a beforeInvocation!");
                }
                else
                    logger.warn("AfterFailure invoked on profile for a registered object, but no profile entry has been set in a beforeInvocation!");
            }
        }
    }

    // ===========================================================================
    @Override
    public void after(Object o, Method m, Object[] params)
    {
    }

    // ===========================================================================
    /**
     * Starts profiling the given method of the given object.
     *
     * @param o The object to profile.
     * @param method The method to profile.
     */
    void startProfiling(Object o, String method)
    {
        Set<String> set = toProfile.get(o.getClass());
        if (set == null)
        {
            set = new HashSet<String>();
            toProfile.put(o.getClass(), set);
        }

        set.add(method);
    }

    // ===========================================================================
    /**
     * Stops profiling the given method of the given object.
     *
     * @param o The object to profile.
     * @param method The method to profile.
     */
    void stopProfiling(Object o, String method)
    {
        Set<String> set = toProfile.get(o.getClass());
        if (set != null)
        {
            set.remove(method);
            if (set.isEmpty()) toProfile.remove(o.getClass());
        }
    }

    // ===========================================================================
    /**
     * Returns the set of methods being profiled for the given object.
     *
     * @param o The object to check.
     * @return The set of methods being profiled.
     */
    Set<String> getEntries(Object o)
    {
        return toProfile.get(o.getClass());
    }
}
