/**************************************************************************
 *
 * Gluewine JUnit Module
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
package org.gluewine.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.core.glue.RepositoryImpl;
import org.gluewine.utils.ErrorLogger;

/**
 * Base class that can be extended by real test classes.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class GluewineTestService
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
     * The set of active servies.
     */
    private Set<Object> services = new HashSet<Object>();

    /**
     * The Gluewine repository to use.
     */
    private Repository repos = RepositoryImpl.getInstance();

    /**
     * Flag indicating that the gluewine test environment has been initialized.
     */
    private boolean initialized = false;

    // ===========================================================================
    /**
     * Closes the gluewine test environment.
     */
    public void closeGluewine()
    {
        // Remove all registered services:
        for (Object o : services)
            repos.unregister(o);

        // Remove all registered listeners:
        for (Object o : services)
            if (o instanceof RepositoryListener<?>)
                repos.removeListener((RepositoryListener<?>) o);

        deactivate();

        initialized = false;
    }

    // ===========================================================================
    /**
     * Initializes the gluewine aspects.
     */
    protected void initGluewine()
    {
        if (initialized) closeGluewine();

        glue();
        registerAllServices();
        activate();
    }

    // ===========================================================================
    /**
     * Registers all active services that implement the RepositoryListener with
     * the repository.
     */
    private void registerAllServices()
    {
        for (Object o : services)
            registerObject(o);
    }

    // ===========================================================================
    /**
     * Registers the given object with the Repository. If the object implements
     * RepositoryListener, it is registered as a listener as well.
     *
     * @param o The object to register.
     */
    private void registerObject(Object o)
    {
        repos.register(o);

        if (o instanceof RepositoryListener<?>)
            repos.addListener((RepositoryListener<?>) o);
    }

    // ===========================================================================
    /**
     * Adds a service.
     *
     * @param o The service to add.
     */
    protected void addService(Object o)
    {
        services.add(o);
    }

    // ===========================================================================
    /**
     * Deactivates all services.
     */
    private void deactivate()
    {
        for (Object o : services)
            deactivate(o);
    }

    // ===========================================================================
    /**
     * Deactivates the embedded service by invoking all methods that have the
     * {@link RunOnDeactivate} annotation.
     *
     * Note that this method will do nothing if isActive() returns false.
     *
     * @param o the service to deactivate.
     */
    private void deactivate(Object o)
    {
        for (final Method method : o.getClass().getMethods())
        {
            if (getRunOnDeactivate(o.getClass(), method) != null)
            {
                try
                {
                    method.invoke(o, new Object[0]);
                }
                catch (Throwable e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Activates all services.
     */
    private void activate()
    {
        for (Object o : services)
            activate(o);
    }

    // ===========================================================================
    /**
     * Activates a service.
     *
     * @param o The service to activate.
     */
    private void activate(Object o)
    {
        for (final Method method : o.getClass().getMethods())
        {
            RunOnActivate annot = getRunOnActivate(o.getClass(), method);
            if (annot != null && method.getParameterTypes().length == 0)
            {
                Runnable r = new ThreadedInvoker(method, o);
                if (annot.runThreaded()) new Thread(r).start();
                else r.run();
            }
        }
    }

    // ===========================================================================
    /**
     * Glues all registered services.
     */
    private void glue()
    {
        for (Object o : services)
            glue(o);
    }

    // ===========================================================================
    /**
     * Glues the specified object.
     *
     * @param o The object to glue.
     */
    private void glue(Object o)
    {
        for (Field field : getAllFields(o.getClass()))
        {
            Glue glue = field.getAnnotation(Glue.class);
            if (glue != null)
            {
                if (Properties.class.isAssignableFrom(field.getType()))
                {
                    String name = glue.properties();
                    if (name != null && name.trim().length() > 0)
                    {
                        Properties props = getProperties(name);
                        assignValue(field, o, props);
                    }
                }
                else
                {
                    for (Object i : services)
                        if (field.getType().isInstance(i))
                            assignValue(field, o, i);
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Returns the properties with the name specified.
     *
     * @param name The name of the properties to load.
     * @return The properties.
     */
    protected Properties getProperties(String name)
    {
        String cfgDir = System.getProperty("cfg.dir");
        if (cfgDir != null)
        {
            File f = new File(cfgDir, name);
            Properties props = new Properties();
            FileInputStream in = null;
            try
            {
                in = new FileInputStream(f);
                props.load(in);
                return props;
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        else throw new RuntimeException("The cfg.dir is not specified as a system property!");
    }

    // ===========================================================================
    /**
     * Assigns the value given to the field of the specified object.
     *
     * @param f The field to process.
     * @param o The owner of the field.
     * @param value The value to assign.
     */
    private void assignValue(Field f, Object o, Object value)
    {
        try
        {
            if (value == null) throw new Throwable("Unresolved field " + f.getName() + " for object " + o.getClass().getName());
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            f.set(o, value);
            f.setAccessible(accessible);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
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
}
