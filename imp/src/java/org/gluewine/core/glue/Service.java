package org.gluewine.core.glue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.gluewine.core.Glue;
import org.gluewine.core.NoSuchServiceException;
import org.gluewine.core.RunBeforeUngluing;
import org.gluewine.core.RunWhenGlued;
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
     * The 'Actual' service.
     */
    private Object actual = null;

    /**
     * Flag indicating that the service is fully resolved.
     */
    private boolean resolved = false;

    /**
     * Flag indicating that the service is fully glued.
     */
    private boolean glued = false;

    /**
     * Flag indicating that the service has been activated.
     */
    private boolean active = false;

    /**
     * The map of references services indexed on the field of the referencing service.
     */
    private Map<Field, Object> references = new HashMap<Field, Object>();

    /**
     * The set of unresolved field names.
     */
    private Set<String> unresolvedFields = new HashSet<String>();

    /**
     * The service id.
     */
    private int id = 0;

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
     * Creates an instance using the embedded service specified.
     *
     * @param service The actual service.
     * @param id The id.
     */
    Service(Object service, int id)
    {
        this.actual = service;
        this.id = id;
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
     * Resolves the service using the given list of services, and returns true
     * if all fields were resolved.
     *
     * @param services The list of services to use.
     * @param providers The set of remote service providers.
     * @return True if all references have been resolved.
     */
    boolean resolve(List<Object> services, Set<ServiceProvider> providers)
    {
        if (!isResolved())
        {
            resolved = true;
            for (Field field : getAllFields(actual.getClass()))
            {
                Glue glue = field.getAnnotation(Glue.class);
                if (glue != null)
                {
                    boolean fieldResolved = false;
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

                    resolved &= fieldResolved;
                }
            }
        }

        return resolved;
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
        if (isResolved() && !isGlued())
        {
            glued = true;
            for (Entry<Field, Object> e : references.entrySet())
            {
                boolean fieldGlued = false;
                try
                {
                    Field field = e.getKey();
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(actual, e.getValue());
                    fieldGlued = true;
                    field.setAccessible(accessible);
                }
                catch (Throwable t)
                {
                    ErrorLogger.log(getClass(), t);
                }

                glued &= fieldGlued;
            }
        }

        return glued;
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
            for (Entry<Field, Object> e : references.entrySet())
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
     * Activates the embedded service by invoking all methods that have the
     * @RunWhenGlued annotation.
     *
     * Note that this method will do nothing if isGlued() returns false.
     */
    boolean activate()
    {
        if (isGlued() && !isActive())
        {
            active = true;
            for (final Method method : actual.getClass().getMethods())
            {
                RunWhenGlued annot = getRunWhenGlued(actual.getClass(), method);
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
     * Deactivates the embedded service by invoking all methods that have the
     * @RunBeforeUngluing annotation.
     *
     * Note that this method will do nothing if isActive() returns false.
     */
    void deactivate()
    {
        if (isActive())
        {
            for (final Method method : actual.getClass().getMethods())
            {
                if (getRunBeforeUngluing(actual.getClass(), method) != null)
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
     * Returns the RunBeforeUngluing annotation if present in the class specified
     * (either in the class given, or one of its parents).
     * If the method hasn't got the annotation, null is returned.
     *
     * @param cl The class to process.
     * @param m The method to check.
     * @return The (possibly bnull) annotation.
     */
    private RunBeforeUngluing getRunBeforeUngluing(Class<?> cl, Method m)
    {
        RunBeforeUngluing annot = null;
        Class<?> c = cl;
        while (annot == null && m != null && c != null)
        {
            annot = m.getAnnotation(RunBeforeUngluing.class);
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
     * Returns the RunWhenGlued annotation if present in the class specified
     * (either in the class given, or one of its parents).
     * If the method hasn't got the annotation, null is returned.
     *
     * @param cl The class to process.
     * @param m The method to check.
     * @return The (possibly bnull) annotation.
     */
    private RunWhenGlued getRunWhenGlued(Class<?> cl, Method m)
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
     * Returns the name of the service. This removes the 'enchanced' part of the
     * name.
     *
     * @return The name of the service.
     */
    public String getName()
    {
        String name = actual.getClass().getName();
        int cgl_i = name.indexOf("$$Enhancer");
        int dscl_i = name.indexOf("Enhanced");
        boolean enhanced = dscl_i > 0;
        enhanced |= cgl_i > 0;

        if (enhanced)
        {
            if (dscl_i > 0) name = name.substring(0, dscl_i);
            else if (cgl_i > 0) name = name.substring(0, cgl_i);
        }

        return name;
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
        int cgl_i = name.indexOf("$$Enhancer");
        int dscl_i = name.indexOf("Enhanced");
        return dscl_i > 0 || cgl_i > 0;
    }
}
