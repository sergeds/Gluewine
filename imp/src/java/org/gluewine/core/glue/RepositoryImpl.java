/**************************************************************************
 *
 * Gluewine Core Module
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
package org.gluewine.core.glue;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;

/**
 * Default implementation of the Registry.
 * It is being "fed" by the Activator.
 *
 * It can be obtained by specifying a Registry member
 * with the @Inject annotation.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class RepositoryImpl implements Repository
{
    // ===========================================================================
    /**
     * The set of registered objects.
     */
    private Set<Object> objects = new HashSet<Object>();

    /**
     * The set of registered listeners.
     */
    private Set<RepositoryListener<? extends Object>> listeners = new HashSet<RepositoryListener<? extends Object>>();

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The instance.
     */
    private static RepositoryImpl instance;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    private RepositoryImpl()
    {

    }

    // ===========================================================================
    @Override
    public synchronized void register(Object o)
    {
        if (objects.add(o))
        {
            logger.debug("Registered object: " + o.getClass().getName());
            for (RepositoryListener<?> l : listeners)
                registered(o, l, getGenericListenerType(l));
        }
    }

    // ===========================================================================
    /**
     * Returns the instance.
     *
     * @return The instance.
     */
    public static synchronized RepositoryImpl getInstance()
    {
        if (instance == null) instance = new RepositoryImpl();
        return instance;
    }

    // ===========================================================================
    /**
     * Invokes the registerd() method of the given listener if the object registered
     * matches the generic signature of the listener.
     *
     * @param o The object that has been registered.
     * @param l The listener to notify.
     * @param generic The generic parameter.
     */
    @SuppressWarnings("unchecked")
    private void registered(Object o, RepositoryListener<?> l, Class<?> generic)
    {
        if (generic != null && generic.isAssignableFrom(o.getClass()))
            ((RepositoryListener<Object>) l).registered(o);
    }

    // ===========================================================================
    /**
     * Invokes the registerd() method of the given listener if the object registered
     * matches the generic signature of the listener.
     *
     * @param o The object that has been registered.
     * @param l The listener to notify.
     * @param generic The generic parameter.
     */
    @SuppressWarnings("unchecked")
    private void unregistered(Object o, RepositoryListener<?> l, Class<?> generic)
    {
        if (generic != null && generic.isAssignableFrom(o.getClass()))
            ((RepositoryListener<Object>) l).unregistered(o);
    }

    // ===========================================================================
    /**
     * Returns the type the generic listener is listening to.
     *
     * @param listener The listener to process.
     * @return The type.
     */
    private Class<?> getGenericListenerType(Object listener)
    {
        /*
         * The reason this method is implemented by inspecting all available methods is
         * that when using the CGLIB enhancer, that classes are not really subclassed but
         * facaded which results in the loss of the generic declaration.
         *
         * The 'method' way works in all cases:
         * - non enhanced
         * - DSCL enhancement
         * - CGLIB enhancement
         */
        Class<?> gen = null;
        Method[] ms = listener.getClass().getMethods();
        boolean found = false;
        for (int i = 0; i < ms.length && !found; i++)
        {
            if (ms[i].getName().equals("registered") && ms[i].getParameterTypes().length == 1)
            {
                /*
                 * Remark: when the class implements the repositoryListener for something else
                 *         than Object, there will be 2 versions available of this method. (type erasure).
                 *         One that accepts Object.class and one that accepts the 'generic' type defined.
                 */
                gen = ms[i].getParameterTypes()[0];
                if (!gen.equals(Object.class))
                    found = true;
            }
        }

        return gen;
    }

    // ===========================================================================
    @Override
    public synchronized void unregister(Object o)
    {
        if (objects.remove(o))
        {
            logger.debug("Unregistered object: " + o.getClass().getName());
            for (RepositoryListener<?> l : listeners)
                unregistered(o, l, getGenericListenerType(l));
        }
    }

    // ===========================================================================
    @Override
    public synchronized void addListener(RepositoryListener<?> listener)
    {
        logger.debug("Registered listener: " + listener.getClass().getName());

        listeners.add(listener);
        Class<?> generic = getGenericListenerType(listener);
        for (Object o : objects)
            registered(o, listener, generic);
    }

    // ===========================================================================
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> c)
    {
        T instance = null;

        Iterator<Object> iter = objects.iterator();
        while (iter.hasNext() && instance == null)
        {
            Object o = iter.next();
            if (c.isAssignableFrom(o.getClass()))
                instance = (T) o;

            /*
            Set<Class<?>> interf = getInterfaces(o.getClass(), null);
            Iterator<Class<?>> iiter = interf.iterator();
            while (iiter.hasNext() && instance == null)
            {
                Class<?> i = iiter.next();
                if (c.isAssignableFrom(i))
                    instance = (T) o;
            }
            */
        }

        return instance;
    }

    // ===========================================================================
    /**
     * Returns the set of interfaces implemented by the class (and all
     * of its parent classes).
     *
     * @param c The class to process.
     * @param set The set to update. If null a set is created.
     * @return The set of unique classnames.
     */
    @SuppressWarnings("unused")
    private Set<Class<?>> getInterfaces(Class<?> c, Set<Class<?>> set)
    {
        if (set == null) set = new HashSet<Class<?>>();

        for (Class<?> interf : c.getInterfaces())
            set.add(interf);

        if (c.getSuperclass() != null)
            getInterfaces(c.getSuperclass(), set);

        return set;
    }

    // ===========================================================================
    @Override
    public synchronized void removeListener(RepositoryListener<?> listener)
    {
        logger.debug("Deregistered listener: " + listener.getClass().getName());
        listeners.remove(listener);
    }

    // ===========================================================================
    /**
     * Returns the number of registered object.
     *
     * @return The number of objects.
     */
    public int getRegisteredObjectCount()
    {
        return objects.size();
    }

    // ===========================================================================
    /**
     * Returns the number of registered listeners.
     *
     * @return The number of listeners.
     */
    public int getRegisteredListenerCount()
    {
        return listeners.size();
    }

    // ===========================================================================
    /**
     * Returns the set of registered objects.
     *
     * @return The set of objects.
     */
    public Set<String> getRegisteredObjects()
    {
        TreeSet<String> s = new TreeSet<String>();
        synchronized (objects)
        {
            for (Object o : objects)
                s.add(o.toString());
        }
        return s;
    }

    // ===========================================================================
    /**
     * Returns the map of registered objects sorted on the toString() of the
     * objects.
     *
     * @return The map of objects.
     */
    public Map<String, Object> getRegisteredObjectMap()
    {
        Map<String, Object> m = new TreeMap<String, Object>();
        synchronized (objects)
        {
            for (Object o : objects)
                m.put(o.toString(), o);
        }
        return m;
    }

    // ===========================================================================
    /**
     * Returns the set of registered listeners.
     *
     * @return The set of listeners.
     */
    public Set<RepositoryListener<?>> getRegisteredListeners()
    {
        Set<RepositoryListener<?>> s = new HashSet<RepositoryListener<?>>();
        s.addAll(listeners);
        return s;
    }

    // ===========================================================================
    /**
     * Removes all registered objects and listeners that matches start with the
     * given name.
     *
     * @param name The name to be matched.
     */
    void remove(String name)
    {
        synchronized (listeners)
        {
            Iterator<RepositoryListener<?>> liter = listeners.iterator();
            while (liter.hasNext())
            {
                if (liter.next().getClass().getName().startsWith(name))
                    liter.remove();
            }

            synchronized (objects)
            {
                Iterator<Object> oiter = objects.iterator();
                while (oiter.hasNext())
                {
                    if (oiter.next().getClass().getName().startsWith(name))
                        oiter.remove();
                }
            }
        }
    }
}
