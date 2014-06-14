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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.ServiceProvider;
import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.CodeSourceListener;
import org.gluewine.launcher.Launcher;
import org.gluewine.launcher.ShutdownListener;
import org.gluewine.utils.ErrorLogger;

/**
 * Glues the classes defined in the manifest of every jar/zip file
 * in the lib folder together.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Gluer implements CodeSourceListener, RepositoryListener<CodeSourceListener>, ShutdownListener
{
    // ===========================================================================
    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The map of service ids.
     */
    private HashMap<String, Integer> serviceIds = new HashMap<String, Integer>();

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
    private RepositoryImpl repository = RepositoryImpl.getInstance();

    /**
     * The Set containing the id's of manually stopped services.
     */
    private HashSet<Integer> stoppedServices = new HashSet<Integer>();

    /**
     * The Set containing the id's of manually unglued services.
     */
    private HashSet<Integer> ungluedServices = new HashSet<Integer>();

    /**
     * The Set containing the id's of manually unresolved services.
     */
    private HashSet<Integer> unresolvedServices = new HashSet<Integer>();

    /**
     * Flag indicating that shutdown has already been initiated.
     */
    private boolean shutdownInitiated = false;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * This will effectively start the glue process.
     * @throws Throwable If an error occurs initializing the gluer.
     */
    @SuppressWarnings("unchecked")
    private Gluer() throws Throwable
    {
        display("----------------------------------------------------------");
        display("           Gluewine Framework - (c) FKS 2013");
        display("                   www.gluewine.org");
        display("----------------------------------------------------------");
        display("Starting framework...");
        long start = System.currentTimeMillis();
        logger.debug("Starting Gluer");

        Map<String, Serializable> persistentMap = Launcher.getInstance().getPersistentMap();
        if (persistentMap.containsKey("GLUE::STOPPED"))
            stoppedServices = (HashSet<Integer>) persistentMap.get("GLUE::STOPPED");
        else persistentMap.put("GLUE::STOPPED", stoppedServices);

        if (persistentMap.containsKey("GLUE::UNGLUED"))
            ungluedServices = (HashSet<Integer>) persistentMap.get("GLUE::UNGLUED");
        else persistentMap.put("GLUE::UNGLUED", ungluedServices);

        if (persistentMap.containsKey("GLUE::UNRESOLVED"))
            unresolvedServices = (HashSet<Integer>) persistentMap.get("GLUE::UNRESOLVED");
        else persistentMap.put("GLUE::UNRESOLVED", unresolvedServices);

        if (persistentMap.containsKey("GLUE::SERVICEIDS"))
            serviceIds = (HashMap<String, Integer>) persistentMap.get("GLUE::SERVICEIDS");
        else persistentMap.put("GLUE::SERVICEIDS", serviceIds);

        if (persistentMap.containsKey("GLUE::NEXTID"))
            nextId = ((Integer) persistentMap.get("GLUE::NEXTID")).intValue();

        loadEnhancer();

        if (enhancer != null) display("Using enhancer: " + enhancer.getClass().getName());
        else display("Running in non-enhanced mode.");

        addService(new Service(Launcher.getInstance(), getServiceId(Launcher.getInstance()), this));
        addService(new Service(repository, getServiceId(repository), this));
        addService(new Service(this, getServiceId(this), this));
        addService(new Service(interceptor, getServiceId(interceptor), this));

        Launcher.getInstance().addShutdownListener(this);

        processSources(Launcher.getInstance().getSources());
        launch();

        persistentMap.put("GLUE::NEXTID", Integer.valueOf(nextId));
        Launcher.getInstance().savePersistentMap();

        display("Gluewine Framework started in " + (System.currentTimeMillis() - start) + " milliseconds.");

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            // ===========================================================================
            @Override
            public void run()
            {
                shutdown();
            }
        });
    }

    // ===========================================================================
    /**
     * Returns the id of the given object.
     *
     * @param service The service to process.
     * @return The id.
     */
    private synchronized int getServiceId(Object service)
    {
        StringBuilder b = new StringBuilder();
        Class<?> cl = service.getClass();
        String name = cl.getName();

        if (name.indexOf("$$EnhancerByCGLIB$$") >= 0)
            cl = service.getClass().getSuperclass();

        ClassLoader loader = cl.getClassLoader();
        if (loader != null)
            b.append(cl.getClassLoader().toString());
        else
            b.append("system");

        b.append("::").append(cl.getName());

        String clid = b.toString();
        int id = 0;
        if (serviceIds.containsKey(clid)) id = serviceIds.get(clid).intValue();
        else
        {
            id = ++nextId;
            serviceIds.put(clid, Integer.valueOf(id));
        }

        return id;
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
     * Will stop and unregister all services that match the given ids.
     * All services referencing the service being stopped, will be stopped as well.
     *
     * @param ids The ids of the services to stop.
     * @param persistState If true, the state of the stopped services is persisted.
     * @return The set of services that have been stopped.
     */
    public Set<Service> stop(int[] ids, boolean persistState)
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

                if (persistState) stoppedServices.add(Integer.valueOf(s.getId()));

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

        Launcher.getInstance().savePersistentMap();

        return stopped;
    }

    // ===========================================================================
    /**
     * Unglues the services with the given ids. The services are first being
     * stopped. Referencing services are unglued as well.
     *
     * @param ids The ids of the service(s) to unglue.
     * @param persistState If true the state of the unglued services is persisted.
     * @return The set of services that were unglued.
     */
    public Set<Service> unglue(int[] ids, boolean persistState)
    {
        Set<Service> unglued = new HashSet<Service>();
        Set<Service> stopped = stop(ids, persistState);

        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && s.isGlued())
            {
                if (persistState) ungluedServices.add(Integer.valueOf(s.getId()));
                s.unglue();
                unglued.add(s);
            }
        }

        for (Service s : stopped)
        {
            if (s.isGlued())
            {
                s.unglue();
                unglued.add(s);
            }
        }

        Launcher.getInstance().savePersistentMap();

        return unglued;
    }

    // ===========================================================================
    /**
     * Unresolves the services that match the ids. The services are first being
     * unglued. Referencing services are unresolved as well.
     *
     * @param ids The ids of the service(s) to unresolve.
     * @param persistState If true the state of the unresolved services is persisted.
     * @return The set of services that were unresolve.
     */
    public Set<Service> unresolve(int[] ids, boolean persistState)
    {
        Set<Service> unresolved = new HashSet<Service>();
        Set<Service> unglued = unglue(ids, persistState);

        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
                if (persistState) unresolvedServices.add(Integer.valueOf(s.getId()));
            }
        }

        for (Service s : unglued)
        {
            if (s.isResolved())
            {
                s.unresolve();
                unresolved.add(s);
            }
        }

        Launcher.getInstance().savePersistentMap();

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
                logger.debug("ClassLoader of service " + s.getActualService().getClass().getName() + " has been removed!");
                unresolve(new int[] {s.getId()}, false);
                siter.remove();
            }
        }

        for (Object o : repository.getRegisteredObjectMap().values())
        {
            if (getClassLoaderForObject(o) == loader)
            {
                logger.debug("ClassLoader of registered object " + o.getClass().getName() + " has been removed!");
                repository.unregister(o);
            }
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
            if (o.getClass().getName().indexOf("$$Enhancer") >= 0)
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

                stoppedServices.remove(Integer.valueOf(id));
            }
        }

        Launcher.getInstance().savePersistentMap();
        launch();
    }

    // ===========================================================================
    /**
     * Resolves, glues and starts all services with the given id.
     *
     * @param ids The service ids.
     */
    public void glue(int[] ids)
    {
        for (int id : ids)
        {
            Service s = serviceMap.get(Integer.valueOf(id));
            if (s != null && !s.isGlued())
            {
                s.glue();
                ungluedServices.remove(Integer.valueOf(id));
            }
        }

        Launcher.getInstance().savePersistentMap();
        launch();
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

            unresolvedServices.remove(Integer.valueOf(id));
        }

        Launcher.getInstance().savePersistentMap();
        launch();
    }

    // ===========================================================================
    /**
     * Clears the state.
     */
    public void clearState()
    {
        stoppedServices.clear();
        ungluedServices.clear();
        unresolvedServices.clear();
        launch();
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
        if (!resolve()) logger.warn("There are unresolved services!");
        glue();
        activate();
        registerAllServices();
        notifyRegistrations();
    }

    // ===========================================================================
    /**
     * Notifies the services that everything has been registered by invoking the
     * @RunAfterRegistration annotated methods.
     */
    private void notifyRegistrations()
    {
        for (Service s : serviceMap.values())
        {
            if (s.isActive())
                s.runAfterRegistration();
        }
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
            if (!stoppedServices.contains(Integer.valueOf(s.getId())))
            {
                if (s.isGlued() && !s.activate())
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
                unglued = false;
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
            if (!ungluedServices.contains(Integer.valueOf(s.getId())))
            {
                if (s.isResolved() && !s.glue())
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
            if (!unresolvedServices.contains(Integer.valueOf(s.getId())))
            {
                if (!s.resolve(actuals, providers))
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
        {
            if (s.isGlued())
                registerObject(s.getActualService());
        }
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
    public synchronized void shutdown()
    {
        if (!shutdownInitiated)
        {
            logger.info("Initiating shutdown...");
            shutdownInitiated = true;
            deregisterAllObjects();
            deactivate();
            unglue();
            unresolve();
            Launcher.getInstance().savePersistentMap();
            logger.info("Framework shut down.");
        }
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
        for (Service service : serviceMap.values())
            l.add(service.getActualService());
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
                    ErrorLogger.log(getClass(), e);
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
                    {
                        o = clazz.newInstance();
                        interceptor.registered((AspectProvider) o);
                    }

                    else o = enhancer.getEnhanced(clazz);

                    addService(new Service(o, getServiceId(o), this));

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
    /**
     * Returns true if the given object is allowed to be glued.
     * An object is allowed to be glued if it is resolved and has not been put in
     * the ungluedServices set.
     *
     * @param o The object to check.
     * @return True if allowed to be glued.
     */
    boolean isAllowedToGlue(Object o)
    {
        Service s = serviceMap.get(getServiceId(o));
        if (s != null)
            return s.isResolved() && !ungluedServices.contains(Integer.valueOf(s.getId()));

        if (o instanceof Properties)
            return true;

        return false;
    }

    // ===========================================================================
    /**
     * Returns true if the given object is allowed to be resolved.
     * An object is allowed to be resolved if it is has not been put in the
     * unresolvedServices set.
     *
     * @param o The object to check.
     * @return True if allowed to be resolved.
     */
    boolean isAllowedToResolve(Object o)
    {
        Service s = serviceMap.get(getServiceId(o));
        if (s != null)
            return !unresolvedServices.contains(Integer.valueOf(s.getId()));

        if (o instanceof Properties)
            return true;

        return false;
    }

    // ===========================================================================
    /**
     * Returns true if the given object is allowed to be activated.
     * An object is allowed to be activated if it is glued has not been put in the
     * stoppedServices set.
     *
     * @param o The object to check.
     * @return True if allowed to be activated.
     */
    boolean isAllowedToActivate(Object o)
    {
        Service s = serviceMap.get(getServiceId(o));
        if (s != null)
            return s.isGlued() && !stoppedServices.contains(Integer.valueOf(s.getId()));

        if (o instanceof Properties)
            return true;

        return false;
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

    // ===========================================================================
    @Override
    public void registered(CodeSourceListener l)
    {
        Launcher.getInstance().addListener(l);
    }

    // ===========================================================================
    @Override
    public void unregistered(CodeSourceListener l)
    {
        Launcher.getInstance().removeListener(l);
    }
}
