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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.CodeSourceListener;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.ServiceProvider;
import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.Launcher;

/**
 * Glues the classes defined in the manifest of every jar/zip file
 * in the lib folder together.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Gluer implements CodeSourceListener
{
    // ===========================================================================
    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The list of services.
     */
   // private List<Service> services = new ArrayList<Service>();

    /**
     * The map of services indexed on their id.
     */
    private Map<Integer, Service> serviceMap = new HashMap<Integer, Service>();

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
     * The next id for the services.
     */
    private int nextId = 0;

    /**
     * The repository instance to use.
     */
    private RepositoryImpl repository = new RepositoryImpl();

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

        addService(new Service(Launcher.getInstance(), getNextId()));
        addService(new Service(repository, getNextId()));
        addService(new Service(this, getNextId()));

        processSources(Launcher.getInstance().getSources());
        launch();

        System.out.println("Gluewine Framework started in " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    // ===========================================================================
    /**
     * Adds a service to the map of registered services.
     *
     * @param service The service to add.
     */
    private void addService(Service service)
    {
        serviceMap.put(Integer.valueOf(service.getId()), service);
    }

    // ===========================================================================
    /**
     * Returns the next available id.
     *
     * @return The next id.
     */
    private synchronized int getNextId()
    {
        return ++nextId;
    }

    // ===========================================================================
    /**
     * Will stop and unregister all services that match the given ids.
     * All services referencing the service being stopped, will be stopped as well.
     *
     * @param ids The ids of the services to stop.
     * @return The set of services that have been stopped.
     */
    public Set<Service> stop(int[] ids)
    {
        Set<Service> stopped = new HashSet<Service>();
        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && s.isActive())
            {
                deregisterObject(s.getActualService());
                s.deactivate();
                stopped.add(s);

                for (Service ref : serviceMap.values())
                {
                    if (ref.references(s.getActualService()))
                    {
                        deregisterObject(ref.getActualService());
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
     * Unglues the services with the given ids. The services are first being
     * stopped. Referencing services are unglued as well.
     *
     * @param ids The ids of the service(s) to unglue.
     * @return The set of services that were unglued.
     */
    public Set<Service> unglue(int[] ids)
    {
        Set<Service> unglued = new HashSet<Service>();
        Set<Service> stopped = stop(ids);
        for (Service s : stopped)
            if (s.isGlued())
            {
                s.unglue();
                unglued.add(s);
            }

        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && s.isGlued())
            {
                s.unglue();
                unglued.add(s);
            }
        }

        return unglued;
    }

    // ===========================================================================
    /**
     * Unresolves the services that match the ids. The services are first being
     * unglued. Referencing services are unresolved as well.
     *
     * @param ids The ids of the service(s) to unresolve.
     * @return The set of services that were unresolve.
     */
    public Set<Service> unresolve(int[] ids)
    {
        Set<Service> unresolved = new HashSet<Service>();
        Set<Service> unglued = unglue(ids);
        for (Service s : unglued)
            if (s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
            }

        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
            }
        }

        return unresolved;
    }

    // ===========================================================================
    /**
     * Invoked when a classloader has been removed from the framework.
     * This will stop, unglue and unresolve all services that were loaded using
     * the classloader, and clean up the repository by removing all objects
     * and listeners that were loaded with this classloader.
     *
     * @param loader The classloader to check.
     */
    public void removed(ClassLoader loader)
    {
        Iterator<Service> siter = serviceMap.values().iterator();
        while (siter.hasNext())
        {
            Service s = siter.next();

            if (getClassLoaderForObject(s.getActualService()) == loader)
            {
                unresolve(new int[] {s.getId()});
                siter.remove();
            }
        }

        for (Object o : repository.getRegisteredObjectMap().values())
        {
            if (getClassLoaderForObject(o) == loader)
                repository.unregister(o);
        }
    }

    // ===========================================================================
    /**
     * Returns the classloader for the given object.
     * Enhancement is taken into account.
     *
     * @param o The object to process.
     * @return The classloader of that object.
     */
    private ClassLoader getClassLoaderForObject(Object o)
    {
        ClassLoader cl = o.getClass().getClassLoader();
        if (isEnhancedMode())
        {
            if (o.getClass().getName().indexOf("$$Enhancer") > 0)
                cl = o.getClass().getSuperclass().getClassLoader();
        }

        return cl;
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services that given ids.
     *
     * @param ids The ids of the service(s) to start.
     */
    public void start(int[] ids)
    {
        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && !s.isActive())
            {
                s.activate();
                registerObject(s.getActualService());
            }
        }
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services with the given id.
     *
     * @param ids[] The service ids.
     */
    public void glue(int[] ids)
    {
        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && !s.isGlued())
                s.glue();
        }
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services with the given ids.
     *
     * @param ids The ids of the services to resolve.
     */
    public void resolve(int[] ids)
    {
        List<Object> actuals = new ArrayList<Object>(serviceMap.size());
        for (Service s : serviceMap.values())
            actuals.add(s.getActualService());

        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && !s.isResolved())
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
        if (o instanceof RepositoryListener<?>)
            repository.removeListener((RepositoryListener<?>) o);
        repository.unregister(o);
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
        registerAllServices();
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

        for (Service s : serviceMap.values())
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
        for (Service s : serviceMap.values())
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

        for (Service s : serviceMap.values())
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
        for (Service s : serviceMap.values())
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

        for (Service s : serviceMap.values())
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
        List<Object> actuals = new ArrayList<Object>(serviceMap.size());
        for (Service s : serviceMap.values())
            actuals.add(s.getActualService());

        boolean resolved = true;
        for (Service s : serviceMap.values())
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
    private void registerAllServices()
    {
        for (Service s : serviceMap.values())
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
     * Deregisters all active services from the repository.
     */
    private void deregisterAllObjects()
    {
        for (Service s : serviceMap.values())
            deregisterObject(s.getActualService());
    }

    // ===========================================================================
    /**
     * Shuts down the framework. All registered ShutdownListeners will first be
     * notified of the imminent shut down.
     */
    public void shutdown()
    {
        deregisterAllObjects();
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
        List<Service> l = new ArrayList<Service>(serviceMap.size());
        l.addAll(serviceMap.values());
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
        List<Object> l = new ArrayList<Object>(serviceMap.size());
        l.addAll(serviceMap.values());
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
        List<CodeSource> sources = Launcher.getInstance().getSources();
        for (int i = 0; i < sources.size() && enhancer == null; i++)
        {
            CodeSource source = sources.get(i);
            for (int j = 0; j < source.getEnhancers().length && enhancer == null; j++)
            {
                try
                {
                    String enh = source.getEnhancers()[j];
                    logger.debug("Instantiating enchancer " + enh);
                    Class<?> clazz = source.getSourceClassLoader().loadClass(enh);
                    Constructor<?> constructor = clazz.getConstructor(Interceptor.class);
                    enhancer = (ClassEnhancer) constructor.newInstance(interceptor);
                }
                catch (Throwable e)
                {
                    logger.error(e);
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Processes the sources specified.
     *
     * @param sources The sources to process.
     */
    private void processSources(List<CodeSource> sources)
    {
        for (CodeSource source : sources)
        {
            ClassLoader loader = source.getSourceClassLoader();
            for (String cl : source.getServices())
            {
                try
                {
                    logger.debug("Instantiating class " + cl);
                    Class<?> clazz = loader.loadClass(cl);

                    Object o = null;
                    if (enhancer == null || AspectProvider.class.isAssignableFrom(clazz))
                        o = clazz.newInstance();

                    else o = enhancer.getEnhanced(clazz);

                    if (AspectProvider.class.isAssignableFrom(clazz))
                        interceptor.register((AspectProvider) o);

                    addService(new Service(o, getNextId()));

                    if (o instanceof ServiceProvider)
                        providers.add((ServiceProvider) o);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        }
    }

    // ===========================================================================
    @Override
    public void codeSourceAdded(List<CodeSource> sources)
    {
        processSources(sources);
        launch();
    }

    // ===========================================================================
    @Override
    public void codeSourceRemoved(List<CodeSource> sources)
    {
        for (CodeSource source : sources)
            removed(source.getSourceClassLoader());

        launch();
    }
}
