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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.gluewine.core.Glue;
import org.gluewine.core.GluewineProperties;
import org.gluewine.core.NoSuchServiceException;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.core.ServiceProvider;
import org.gluewine.core.utils.ErrorLogger;

/**
 * A bean that wraps a 'glued' service.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Service
{
    // ===========================================================================
    /**
     * Class used to invoked RunWhenGlued method in a separate thread.
     */
    private static class ThreadedInvoker implements Runnable
    {
        /**
         * The method to invoke.
         */
        private Method method = null;

        /**
         * The object to be processed.
         */
        private Object o = null;

        // ===========================================================================
        /**
         * Creates an instance.
         *
         * @param method The method to invoke.
         * @param o The Object to process.
         */
        ThreadedInvoker(Method method, Object o)
        {
            this.method = method;
            this.o = o;
        }

        // ===========================================================================
        @Override
        public void run()
        {
            try
            {
                method.invoke(o, new Object[0]);
            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
            }
        }
    }

    /**
     * Flag indicating that the service has been activated.
     */
    private boolean active = false;

    /**
     * The 'Actual' service.
     */
    private Object actual = null;

    /**
     * Flag indicating that the service is fully glued.
     */
    private boolean glued = false;

    /**
     * The service id.
     */
    private int id = 0;

    /**
     * The logger instance.
     */
    private Logger logger = null;

    /**
     * The map of references services indexed on the field of the referencing service.
     */
    private Map<Field, Object> references = new HashMap<Field, Object>();

    /**
     * Flag indicating that the service is fully resolved.
     */
    private boolean resolved = false;

    /**
     * The set of unresolved field names.
     */
    private Set<String> unresolvedFields = new HashSet<String>();

    /**
     * The gluer instance.
     */
    private transient Gluer gluer = null;

    // ===========================================================================
    /**
     * Creates an instance using the embedded service specified.
     *
     * @param service The actual service.
     * @param id The id.
     * @param gluer The gluer.
     */
    Service(Object service, int id, Gluer gluer)
    {
        this.actual = service;
        this.id = id;
        this.gluer = gluer;
        logger = Logger.getLogger(getName());
    }

    // ===========================================================================
    /**
     * Activates the embedded service by invoking all methods that have the
     * {@link RunOnActivate} annotation.
     *
     * Note that this method will do nothing if isGlued() returns false.
     *
     * @return True if activated.
     */
    boolean activate()
    {
        if (isGlued() && !isActive() && referencesAreAllowedToActivate())
        {
            active = true;
            for (final Method method : actual.getClass().getMethods())
            {
                RunOnActivate annot = getRunOnActivate(actual.getClass(), method);
                if (annot != null && method.getParameterTypes().length == 0)
                {
                    boolean methodActive = false;
                    Runnable r = new ThreadedInvoker(method, actual);
                    if (annot.runThreaded()) new Thread(r).start();
                    else r.run();
                    methodActive = true;
                    active &= methodActive;
                }
            }
        }

        return active;
    }

    // ===========================================================================
    /**
     * Returns true if all contained references are allowed to be
     * activated.
     *
     * @return True if all references can be activated.
     */
    private boolean referencesAreAllowedToActivate()
    {
        boolean ok = true;
        Iterator<Entry<Field, Object>> iter = references.entrySet().iterator();
        while (iter.hasNext() && ok)
        {
            Entry<Field, Object> e = iter.next();
            ok = gluer.isAllowedToActivate(e.getValue());
            if (!ok) logger.debug("Cannot be activated as member : " + e.getKey().getName() + " is not allowed to be activated!");
        }

        return ok;
    }

    // ===========================================================================
    /**
     * Returns true if all contained references are allowed to be
     * glued.
     *
     * @return True if all references can be glued.
     */
    private boolean referencesAreAllowedToGlue()
    {
        boolean ok = true;
        Iterator<Entry<Field, Object>> iter = references.entrySet().iterator();
        while (iter.hasNext() && ok)
        {
            Entry<Field, Object> e = iter.next();
            ok = gluer.isAllowedToGlue(e.getValue());
            if (!ok) logger.debug("Cannot be glued as member : " + e.getKey().getName() + " is not allowed to be glued!");
        }

        return ok;
    }

    // ===========================================================================
    /**
     * Returns true if all contained references are allowed to be
     * resolved.
     *
     * @return True if all references can be resolved.
     */
    private boolean referencesAreAllowedToResolve()
    {
        boolean ok = true;
        Iterator<Entry<Field, Object>> iter = references.entrySet().iterator();
        while (iter.hasNext() && ok)
        {
            Entry<Field, Object> e = iter.next();
            ok = gluer.isAllowedToResolve(e.getValue());
            if (!ok) logger.debug("Cannot be resolved as member : " + e.getKey().getName() + " is not allowed to be resolved!");
        }

        return ok;
    }

    // ===========================================================================
    /**
     * Deactivates the embedded service by invoking all methods that have the
     * {@link RunOnDeactivate} annotation.
     *
     * Note that this method will do nothing if isActive() returns false.
     */
    void deactivate()
    {
        if (isActive())
        {
            for (final Method method : actual.getClass().getMethods())
            {
                if (getRunOnDeactivate(actual.getClass(), method) != null)
                {
                    try
                    {
                        method.invoke(actual, new Object[0]);
                    }
                    catch (Throwable e)
                    {
                        ErrorLogger.log(getClass(), e);
                    }
                }
            }

            active = false;
        }
    }

    // ===========================================================================
    /**
     * Returns the embedded service.
     *
     * @return The embedded service.
     */
    public Object getActualService()
    {
        return actual;
    }

    // ===========================================================================
    /**
     * Returns the list of ALL fields of a class. This is done recursively.
     * Note that overriden fields are excluded.
     *
     * @param clazz The class to process.
     * @return The list of fields.
     */
    private List<Field> getAllFields(Class<?> clazz)
    {
        Map<String, Field> fields = new HashMap<String, Field>();
        Class<?> c = clazz;
        while (c != null)
        {
            for (Field field : c.getDeclaredFields())
            {
                if (!fields.containsKey(field.getName()))
                    fields.put(field.getName(), field);
            }

            c = c.getSuperclass();
        }

        List<Field> l = new ArrayList<Field>(fields.size());
        l.addAll(fields.values());
        return l;
    }

    // ===========================================================================
    /**
     * Returns the id of the service.
     *
     * @return The id.
     */
    public int getId()
    {
        return id;
    }

    // ===========================================================================
    /**
     * Returns the name of the service. This removes the 'enchanced' part of the
     * name.
     *
     * @return The name of the service.
     */
    public String getName()
    {
        String name = actual.getClass().getName();
        int cgli = name.indexOf("$$Enhancer");
        int dscli = name.indexOf("Enhanced");
        boolean enhanced = dscli > 0;
        enhanced |= cgli > 0;

        if (enhanced)
        {
            if (dscli > 0) name = name.substring(0, dscli);
            else if (cgli > 0) name = name.substring(0, cgli);
        }

        return name;
    }

    // ===========================================================================
    /**
     * Returns the RunOnDeactivate annotation if present in the class specified
     * (either in the class given, or one of its parents).
     * If the method hasn't got the annotation, null is returned.
     *
     * @param cl The class to process.
     * @param m The method to check.
     * @return The (possibly bnull) annotation.
     */
    private RunOnDeactivate getRunOnDeactivate(Class<?> cl, Method m)
    {
        RunOnDeactivate annot = null;
        Class<?> c = cl;
        while (annot == null && m != null && c != null)
        {
            annot = m.getAnnotation(RunOnDeactivate.class);
            if (annot == null)
            {
                c = c.getSuperclass();
                try
                {
                    if (c != null)
                        m = c.getMethod(m.getName(), m.getParameterTypes());
                }
                catch (NoSuchMethodException e)
                {
                    m = null;
                }
            }
        }

        return annot;
    }

    // ===========================================================================
    /**
     * Returns the RunOnActivate annotation if present in the class specified
     * (either in the class given, or one of its parents).
     * If the method hasn't got the annotation, null is returned.
     *
     * @param cl The class to process.
     * @param m The method to check.
     * @return The (possibly bnull) annotation.
     */
    private RunOnActivate getRunOnActivate(Class<?> cl, Method m)
    {
        RunOnActivate annot = null;
        Class<?> c = cl;
        while (annot == null && m != null && c != null)
        {
            annot = m.getAnnotation(RunOnActivate.class);
            if (annot == null)
            {
                c = c.getSuperclass();
                try
                {
                    if (c != null)
                        m = c.getMethod(m.getName(), m.getParameterTypes());
                }
                catch (NoSuchMethodException e)
                {
                    m = null;
                }
            }
        }

        return annot;
    }

    // ===========================================================================
    /**
     * Returns the class of the embedded service.
     *
     * @return The class.
     */
    public Class<?> getServiceClass()
    {
        return actual.getClass();
    }

    // ===========================================================================
    /**
     * Returns the set of unresolved field names.
     *
     * @return The set of unresolved field names.
     */
    public Set<String> getUnresolvedFields()
    {
        Set<String> s = new TreeSet<String>();
        s.addAll(unresolvedFields);
        return s;
    }

    // ===========================================================================
    /**
     * Glues all the references services, and returns true if everything was
     * glued. Note that this method will do nothing if isResolved() returns
     * false.
     *
     * @return True if all services have been glued.
     */
    boolean glue()
    {
        if (isResolved() && !isGlued() && referencesAreAllowedToGlue())
        {
            glued = true;
            for (final Entry<Field, Object> e : references.entrySet())
            {
                boolean fieldGlued = false;
                fieldGlued = AccessController.doPrivileged(new PrivilegedAction<Boolean>()
                {
                    @Override
                    public Boolean run()
                    {
                        try
                        {
                            Field field = e.getKey();
                            boolean accessible = field.isAccessible();
                            field.setAccessible(true);
                            field.set(actual, e.getValue());
                            field.setAccessible(accessible);
                            return true;
                        }
                        catch (Throwable t)
                        {
                            ErrorLogger.log(getClass(), t);
                            return false;
                        }
                    }
                }).booleanValue();

                glued &= fieldGlued;
            }
        }

        return glued;
    }

    // ===========================================================================
    /**
     * Returns true if the service is active.
     *
     * @return True if active.
     */
    public boolean isActive()
    {
        return active;
    }

    // ===========================================================================
    /**
     * Returns true if the service has been enhanced.
     *
     * @return True if enhanced.
     */
    public boolean isEnhanced()
    {
        String name = actual.getClass().getName();
        int cgli = name.indexOf("$$Enhancer");
        int dscli = name.indexOf("Enhanced");
        return dscli > 0 || cgli > 0;
    }

    // ===========================================================================
    /**
     * Returns true if the service is glued.
     *
     * @return True if glued.
     */
    public boolean isGlued()
    {
        return glued;
    }

    // ===========================================================================
    /**
     * Returns true if the service is resolved.
     *
     * @return True if resolved.
     */
    public boolean isResolved()
    {
        return resolved;
    }

    // ===========================================================================
    /**
     * Returns true if the embedded service references the given object.
     *
     * @param o The object to check.
     * @return True if referenced.
     */
    boolean references(Object o)
    {
        return references.values().contains(o);
    }

    // ===========================================================================
    /**
     * Resolves the service using the given list of services, and returns true
     * if all fields were resolved.
     *
     * @param services The list of services to use.
     * @param providers The set of remote service providers.
     * @return True if all references have been resolved.
     */
    boolean resolve(List<Object> services, Set<ServiceProvider> providers)
    {
        if (!isResolved() && referencesAreAllowedToResolve())
        {
            resolved = true;
            for (Field field : getAllFields(actual.getClass()))
            {
                Glue glue = field.getAnnotation(Glue.class);
                if (glue != null)
                {
                    boolean fieldResolved = false;
                    if (Properties.class.isAssignableFrom(field.getType()))
                    {
                        String name = glue.properties();
                        if (name != null && name.trim().length() > 0)
                        {
                            try
                            {
                                GluewineProperties props = null;
                                String method = glue.refresh();

                                if (method != null)
                                    props = new GluewineProperties(name, actual, method);
                                else
                                    props = new GluewineProperties(name, actual);

                                props.load();
                                references.put(field, props);
                                fieldResolved = true;
                            }
                            catch (IOException e)
                            {
                                logger.warn("Field " + field.getName() + " of class " + actual.getClass().getName() + " does not exist!");
                                ErrorLogger.log(getClass(), e);
                                unresolvedFields.add(field.getName());
                            }
                        }
                        else
                            unresolvedFields.add(field.getName());
                    }
                    else
                    {
                        // First check the local services:
                        for (Object i : services)
                        {
                            if (field.getType().isInstance(i))
                            {
                                references.put(field, i);
                                fieldResolved = true;
                            }
                        }

                        Iterator<ServiceProvider> provIter = providers.iterator();
                        while (provIter.hasNext() && !fieldResolved)
                        {
                            try
                            {
                                ServiceProvider provider = provIter.next();
                                Object proxy = provider.getService(field.getType());
                                if (proxy != null)
                                {
                                    try
                                    {
                                        references.put(field, proxy);
                                        fieldResolved = true;
                                    }
                                    catch (Throwable e)
                                    {
                                        ErrorLogger.log(getClass(), e);
                                    }
                                }
                            }
                            catch (NoSuchServiceException e)
                            {
                                ErrorLogger.log(getClass(), e);
                                // Allowed to fail as not all providers are
                                // required to be able to provide all services.
                            }
                        }

                        if (!fieldResolved) unresolvedFields.add(field.getName());
                    }
                    resolved &= fieldResolved;
                }
            }
        }

        return resolved;
    }

    // ===========================================================================
    /**
     * Glues all the references services, and returns true if everything was
     * glued. Note that this method will do nothing if isActive() returns true
     * or isGlued() returns false.
     *
     * @return True if unglued.
     */
    boolean unglue()
    {
        if (!isActive() && isGlued())
        {
            for (final Entry<Field, Object> e : references.entrySet())
            {
                AccessController.doPrivileged(new PrivilegedAction<Void>()
                {
                    @Override
                    public Void run()
                    {
                        try
                        {
                            Field field = e.getKey();
                            boolean accessible = field.isAccessible();
                            field.setAccessible(true);
                            field.set(actual, null);
                            field.setAccessible(accessible);
                        }
                        catch (Throwable t)
                        {
                            ErrorLogger.log(getClass(), t);
                        }
                        return null;
                    }
                });
            }
            glued = false;
        }

        return !glued;
    }

    // ===========================================================================
    /**
     * Unresolved the service by clearing all references to other services.
     * This method does nothing if the service is Glued() or not Resolved().
     */
    void unresolve()
    {
        if (!isGlued() && isResolved())
        {
            unresolvedFields.clear();
            references.clear();
            resolved = false;
        }
    }
}
