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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.ServiceProvider;
import org.gluewine.core.utils.ErrorLogger;
import org.gluewine.launcher.Launcher;

/**
 * Glues the classes defined in the manifest of every jar/zip file
 * in the lib folder together.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Gluer
{
    // ===========================================================================
    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The list of services.
     */
    private List<Service> services = new ArrayList<Service>();

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

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * This will effectively start the glue process.
     * @throws Throwable If an error occurs initializing the gluer.
     */
    private Gluer() throws Throwable
    {
        display("----------------------------------------------------------");
        display("           Gluewine Framework - (c) FKS 2013");
        display("                   www.gluewine.org");
        display("----------------------------------------------------------");
        display("Starting framework...");
        long start = System.currentTimeMillis();
        logger.debug("Starting Gluer");

        loadEnhancer();

        if (enhancer != null) display("Using enhancer: " + enhancer.getClass().getName());
        else display("Running in non-enhanced mode.");

        services.add(new Service(Launcher.getInstance()));
        services.add(new Service(repository));
        services.add(new Service(this));

        index();
        launch();

        System.out.println("Gluewine Framework started in " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    // ===========================================================================
    /**
     * Will stop and unregister all services that match the given name.
     * All services referencing the service being stopped, will be stopped as well.
     *
     * @param name The name of the service(s) to stop.
     * @return The set of services that have been stopped.
     */
    public Set<Service> stop(String name)
    {
        Set<Service> stopped = new HashSet<Service>();
        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && s.isActive())
            {
                repository.unregister(s.getActualService());
                s.deactivate();
                stopped.add(s);

                for (Service ref : services)
                {
                    if (ref.references(s.getActualService()))
                    {
                        repository.unregister(ref.getActualService());
                        ref.deactivate();
                        stopped.add(ref);
                    }
                }
            }
        }
        return stopped;
    }

    // ===========================================================================
    /**
     * Unglues the services that match the name. The services are first being
     * stopped. Referencing services are unglued as well.
     *
     * @param name The name of the service(s) to unglue.
     * @return The set of services that were unglued.
     */
    public Set<Service> unglue(String name)
    {
        Set<Service> unglued = new HashSet<Service>();
        Set<Service> stopped = stop(name);
        for (Service s : stopped)
            if (s.isGlued())
            {
                s.unglue();
                unglued.add(s);
            }

        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && s.isGlued())
            {
                s.unglue();
                unglued.add(s);
            }
        }

        return unglued;
    }

    // ===========================================================================
    /**
     * Unresolves the services that match the name. The services are first being
     * unglued. Referencing services are unresolved as well.
     *
     * @param name The name of the service(s) to unresolve.
     * @return The set of services that were unresolve.
     */
    public Set<Service> unresolve(String name)
    {
        Set<Service> unresolved = new HashSet<Service>();
        Set<Service> unglued = unglue(name);
        for (Service s : unglued)
            if (s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
            }

        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
            }
        }

        return unresolved;
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services that match the given name.
     *
     * @param name The name of the service(s) to start.
     */
    public void start(String name)
    {
        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && !s.isActive())
            {
                s.activate();
                registerObject(s.getActualService());
            }
        }
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services that match the given name.
     *
     * @param name The name of the service(s) to start.
     */
    public void glue(String name)
    {
        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && !s.isGlued())
                s.glue();
        }
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services that match the given name.
     *
     * @param name The name of the service(s) to start.
     */
    public void resolve(String name)
    {
        List<Object> actuals = new ArrayList<Object>(services.size());
        for (Service s : services)
            actuals.add(s.getActualService());

        for (Service s : services)
        {
            String serviceName = s.getServiceClass().getName();
            if (serviceName.startsWith(name) && !s.isResolved())
                s.resolve(actuals, providers);
        }
    }

    // ===========================================================================
    /**
     * Deregisters the given object from the repository. If the object
     * is a RepositoryListener, it is removed as a listener as well.
     *
     * @param o The object to deregister.
     */
    private void deregisterObject(Object o)
    {
        repository.unregister(o);
        if (o instanceof RepositoryListener<?>)
            repository.removeListener((RepositoryListener<?>) o);
    }

    // ===========================================================================
    /**
     * Launches the framework.
     */
    private void launch()
    {
        if (!resolve()) warn("There are unresolved services!");
        glue();
        activate();
        registerRepositoryListeners();
    }

    // ===========================================================================
    /**
     * Activates all glued services.
     *
     * @return True if all glued services were activated.
     */
    private boolean activate()
    {
        boolean active = true;

        for (Service s : services)
        {
            if (s.isGlued() && !s.activate())
            {
                warn(s.getActualService().getClass().getName() + " could not be activated !");
                active = false;
            }
        }

        return active;
    }

    // ===========================================================================
    /**
     * Dectivates all activated services.
     */
    private void deactivate()
    {
        for (Service s : services)
        {
            if (s.isActive())
                s.deactivate();
        }
    }

    // ===========================================================================
    /**
     * Unglues all glued services.
     *
     * @return True if all glued services were unglued.
     */
    private boolean unglue()
    {
        boolean unglued = true;

        for (Service s : services)
        {
            if (s.isGlued() && !s.unglue())
            {
                warn(s.getActualService().getClass().getName() + " could not be unglued !");
                unglued = false;
            }
        }

        return unglued;
    }

    // ===========================================================================
    /**
     * Unresolves all resolved services.
     */
    private void unresolve()
    {
        for (Service s : services)
        {
            if (s.isResolved())
                s.unresolve();
        }
    }

    // ===========================================================================
    /**
     * Glues all resolved services.
     *
     * @return True if all resolved services were glued.
     */
    private boolean glue()
    {
        boolean glued = true;

        for (Service s : services)
        {
            if (s.isResolved() && !s.glue())
            {
                warn(s.getActualService().getClass().getName() + " could not be glued !");
                glued = false;
            }
        }

        return glued;
    }

    // ===========================================================================
    /**
     * Resolves all services.
     *
     * @return True if ALL services were resolved.
     */
    private boolean resolve()
    {
        List<Object> actuals = new ArrayList<Object>(services.size());
        for (Service s : services)
            actuals.add(s.getActualService());

        boolean resolved = true;
        for (Service s : services)
        {
            if (!s.resolve(actuals, providers))
            {
                warn(s.getActualService().getClass().getName() + " could not be fully resolved !");
                resolved = false;
            }
        }

        return resolved;
    }

    // ===========================================================================
    /**
     * Outputs a String to StdOut as in the logger.
     *
     * @param s The String to display.
     */
    private void display(String s)
    {
        System.out.println(s);
        logger.info(s);
    }

    // ===========================================================================
    /**
     * Outputs a String to StdOut as in the logger.
     *
     * @param s The String to display.
     */
    private void warn(String s)
    {
        System.out.println(s);
        logger.warn(s);
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
     * Registers all active services that implement the RepositoryListener with
     * the repository.
     */
    private void registerRepositoryListeners()
    {
        for (Service s : services)
            registerObject(s.getActualService());
    }

    // ===========================================================================
    /**
     * Registers the given object with the Repository. If the object implements
     * RepositoryListener, it is registered as a listener as well.
     *
     * @param Object The object to register.
     */
    private void registerObject(Object o)
    {
        repository.register(o);

        if (o instanceof RepositoryListener<?>)
            repository.addListener((RepositoryListener<?>) o);
    }

    // ===========================================================================
    /**
     * Deregisters all active services that implement the RepositoryListener from
     * the repository.
     */
    private void deregisterRepositoryListeners()
    {
        for (Service s : services)
            deregisterObject(s.getActualService());
    }

    // ===========================================================================
    /**
     * Shuts down the framework. All registered ShutdownListeners will first be
     * notified of the imminent shut down.
     */
    public void shutdown()
    {
        deregisterRepositoryListeners();
        deactivate();
        unglue();
        unresolve();
        System.exit(0);
    }

    // ===========================================================================
    /**
     * Returns the list of services.
     *
     * @return The list of services.
     */
    public List<Service> getServices()
    {
        List<Service> l = new ArrayList<Service>(services.size());
        l.addAll(services);
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
                    String enh = attr.getValue("Gluewine-Enhancer");
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
                    String act = attr.getValue("Gluewine-Services");
                    if (act != null)
                    {
                        ClassLoader loader = Launcher.getInstance().getClassLoaderForJar(file);
                        act = act.trim();
                        String[] cl = act.split(",");
                        for (String c : cl)
                        {
                            c = c.trim();
                            logger.debug("Instantiating class " + c);
                            Class<?> clazz = loader.loadClass(c);

                            Object o = null;
                            if (enhancer == null || AspectProvider.class.isAssignableFrom(clazz))
                                o = clazz.newInstance();

                            else
                                o = enhancer.getEnhanced(clazz);

                            if (AspectProvider.class.isAssignableFrom(clazz))
                                interceptor.register((AspectProvider) o);

                            services.add(new Service(o));

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
}
