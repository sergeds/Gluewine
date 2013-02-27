/**************************************************************************
 *
 * Gluewine Core Module
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
package org.gluewine.core.glue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.Glue;
import org.gluewine.core.NoSuchServiceException;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunWhenGlued;
import org.gluewine.core.ServiceProvider;
import org.gluewine.core.ShutdownListener;
import org.gluewine.core.utils.ErrorLogger;
import org.gluewine.launcher.Launcher;

/**
 * Glues the classes defined in the manifest of every jar/zip file
 * in the lib folder together.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Gluer implements RepositoryListener<ShutdownListener>
{
    // ===========================================================================
    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The list of services.
     */
    private List<Object> services = new ArrayList<Object>();

    /**
     * List of objects that have been fully resolved.
     */
    private List<Object> resolved = new ArrayList<Object>();

    /**
     * The set of available service providers.
     */
    private Set<ServiceProvider> providers = new HashSet<ServiceProvider>();

    /**
     * The interceptor to use.
     */
    private Interceptor interceptor = new Interceptor();

    /**
     * The class "enhancer".
     */
    private ClassEnhancer enhancer = null;

    /**
     * The repository instance to use.
     */
    private Repository repository = new RepositoryImpl();

    /**
     * The set of registered shutdown listeners.
     */
    private Set<ShutdownListener> shutdownListeners = new HashSet<ShutdownListener>();

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

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * This will effectively start the glue process.
     * @throws Throwable If an error occurs initializing the gluer.
     */
    private Gluer() throws Throwable
    {
        System.out.println("Gluewine Framework - (c) FKS 2013");
        System.out.println("www.gluewine.org");
        System.out.println("Starting framework...");
        long start = System.currentTimeMillis();
        logger.debug("Starting Injector");

        loadEnhancer();

        repository.addListener(this);
        services.add(Launcher.getInstance());
        services.add(repository);
        services.add(this);

        index();
        glue();

        if (resolved.size() != services.size())
        {
            logger.warn("There are unresolved services !");
            for (Object o : services)
            {
                if (!resolved.contains(o))
                    logger.warn("UNRESOLVED: " + o.getClass().getName());
            }
        }

        initiate();
        registerRepositoryListeners();

        System.out.println("Gluewine Framework started in " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    // ===========================================================================
    /**
     * Main invocation routine.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            new Gluer();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    /**
     * Registers all services that implement the RegistryListener
     * with the registry.
     */
    private void registerRepositoryListeners()
    {
        for (Object o : resolved)
        {
            repository.register(o);
            if (o instanceof RepositoryListener<?>)
                repository.addListener((RepositoryListener<?>) o);
        }

        repository.register(this);
    }

    // ===========================================================================
    /**
     * Shuts down the framework. All registered ShutdownListeners will first be
     * notified of the imminent shut down.
     */
    public void shutdown()
    {
        Set<ShutdownListener> toShutdown = new HashSet<ShutdownListener>(shutdownListeners.size());
        toShutdown.addAll(shutdownListeners);
        for (ShutdownListener l : toShutdown)
        {
            try
            {
                l.shuttingDown();
            }
            catch (Throwable e)
            {
                logger.error(e);
            }
        }

        System.exit(0);
    }

    // ===========================================================================
    /**
     * Invokes the <b>RunAfterInjection</b> annotated methods on all services
     * that were resolved.
     */
    private void initiate()
    {
        for (final Object o : resolved)
        {
            for (final Method method : o.getClass().getMethods())
            {
                RunWhenGlued annot = getRunAfterInjection(o.getClass(), method);
                if (annot != null && method.getParameterTypes().length == 0)
                {
                    Runnable r = new ThreadedInvoker(method, o);
                    if (annot.runThreaded()) new Thread(r).start();
                    else r.run();
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Returns the list of active services.
     *
     * @return The list of services.
     */
    public List<Object> getActiveServices()
    {
        List<Object> l = new ArrayList<Object>(resolved.size());
        l.addAll(resolved);
        return l;
    }

    // ===========================================================================
    /**
     * Returns the list of defined services.
     *
     * @return The list of services.
     */
    public List<Object> getDefinedServices()
    {
        List<Object> l = new ArrayList<Object>(services.size());
        l.addAll(services);
        return l;
    }

    // ===========================================================================
    /**
     * Returns true if the framework is running in 'Enhanced Mode'. Enhanced mode
     * means that all active services have been extended allowing for AspectProviders
     * to be invoked before and after all public methods.
     *
     * If false is returned, it means that no enhancers were available, and that
     * all services are running as is.
     *
     * @return True if enhanced.
     */
    public boolean isEnhancedMode()
    {
        return enhancer != null;
    }

    // ===========================================================================
    /**
     * Returns the RunAfterInjection annotation if present in the class specified
     * (either in the class given, or one of its parents).
     * If the method hasn't got the annotation, null is returned.
     *
     * @param cl The class to process.
     * @param m The method to check.
     * @return The (possibly bnull) annotation.
     */
    private RunWhenGlued getRunAfterInjection(Class<?> cl, Method m)
    {
        RunWhenGlued annot = null;
        Class<?> c = cl;
        while (annot == null && m != null && c != null)
        {
            annot = m.getAnnotation(RunWhenGlued.class);
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
     * Glues all members.
     */
    private void glue()
    {
        for (Object o : services)
        {
            boolean glued = true;
            for (Field field : getAllFields(o.getClass()))
            {
                Glue glue = field.getAnnotation(Glue.class);
                if (glue != null)
                {
                    boolean fieldGlued = false;
                    // First check the local services:
                    for (Object i : services)
                    {
                        if (field.getType().isInstance(i))
                            try
                            {
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(o, i);
                                fieldGlued = true;
                                field.setAccessible(accessible);
                            }
                            catch (Throwable e)
                            {
                                ErrorLogger.log(getClass(), e);
                            }
                    }

                    if (!fieldGlued)
                    {
                        // Check the remote providers:
                        Iterator<ServiceProvider> provIter = providers.iterator();
                        while (provIter.hasNext() && !fieldGlued)
                        {
                            try
                            {
                                ServiceProvider provider = provIter.next();
                                Object proxy = provider.getService(field.getType());
                                if (proxy != null)
                                {
                                    try
                                    {
                                        boolean accessible = field.isAccessible();
                                        field.setAccessible(true);
                                        field.set(o, proxy);
                                        fieldGlued = true;
                                        field.setAccessible(accessible);
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
                        if (!fieldGlued)
                            logger.warn("Could not set " + field.getType() + " to " + o.getClass().getName());
                    }

                    glued &= fieldGlued;
                }
            }

            if (glued)
                resolved.add(o);
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
     * Looks for an enhancer and if found uses it. The search will stop when the
     * first enhancer is encountered.
     */
    private void loadEnhancer()
    {
        List<File> files = Launcher.getInstance().getJarFiles();
        for (int i = 0; i < files.size() && enhancer == null; i++)
        {
            File file = files.get(i);
            JarFile jar = null;
            try
            {
                jar = new JarFile(file);
                Manifest manifest = jar.getManifest();
                if (manifest != null)
                {
                    Attributes attr = manifest.getMainAttributes();
                    String enh = attr.getValue("gluewine-enhancer");
                    if (enh != null)
                    {
                        enh = enh.trim();
                        logger.debug("Instantiating enchancer " + enh);
                        Class<?> clazz = getClass().getClassLoader().loadClass(enh);
                        Constructor<?> constructor = clazz.getConstructor(Interceptor.class);
                        enhancer = (ClassEnhancer) constructor.newInstance(interceptor);
                    }
                }
            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
                throw new RuntimeException(e);
            }
            finally
            {
                if (jar != null)
                {
                    try
                    {
                        jar.close();
                    }
                    catch (Throwable e)
                    {
                        ErrorLogger.log(getClass(), e);
                    }
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Indexes all jar/zip files and checks the manifests.
     */
    private void index()
    {
        for (File file : Launcher.getInstance().getJarFiles())
        {
            JarFile jar = null;
            try
            {
                jar = new JarFile(file);
                Manifest manifest = jar.getManifest();
                if (manifest != null)
                {
                    Attributes attr = manifest.getMainAttributes();
                    String act = attr.getValue("gluewine-glue");
                    if (act != null)
                    {
                        act = act.trim();
                        String[] cl = act.split(",");
                        for (String c : cl)
                        {
                            c = c.trim();
                            logger.debug("Instantiating class " + c);
                            Class<?> clazz = getClass().getClassLoader().loadClass(c);

                            Object o = null;
                            if (enhancer == null || AspectProvider.class.isAssignableFrom(clazz))
                                o = clazz.newInstance();

                            else
                                o = enhancer.getEnhanced(clazz);

                            if (AspectProvider.class.isAssignableFrom(clazz))
                                interceptor.register((AspectProvider) o);

                            services.add(o);

                            if (o instanceof ServiceProvider)
                                providers.add((ServiceProvider) o);
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
                throw new RuntimeException(e);
            }
            finally
            {
                if (jar != null)
                {
                    try
                    {
                        jar.close();
                    }
                    catch (Throwable e)
                    {
                        ErrorLogger.log(getClass(), e);
                    }
                }
            }
        }
    }

    // ===========================================================================
    @Override
    public void registered(ShutdownListener l)
    {
        shutdownListeners.add(l);
    }

    // ===========================================================================
    @Override
    public void unregistered(ShutdownListener l)
    {
        shutdownListeners.remove(l);
    }
}
