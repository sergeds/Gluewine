package org.gluewine.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Static helper class that handles annotations searches.
 * Ie. requests for annotations are also dispatched to interfaces.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class AnnotationUtility
{
    // ===========================================================================
    /**
     * Use the static methods.
     */
    private AnnotationUtility()
    {
    }

    // ===========================================================================
    /**
     * Looks for the annotation specified on the given object. Remark that if
     * the annotation is not @Inherited, the call is only dispatched to the
     * object's class and all its interfaces. Not to its superclasses.
     *
     * If no such annotation could be found, null is returned.
     *
     * @param cl The annotation class to look for.
     * @param o The object to parse.
     * @return The (possibly null) annotation instance.
     */
    public static <T extends Annotation> T getAnnotation(Class<T> cl, Object o)
    {
        return getAnnotation(cl, o.getClass());
    }

    // ===========================================================================
    /**
     * Looks for the annotation specified in the given class. All the interfaces
     * the class implements are also being searched.
     *
     * @param ann The annotation to look for.
     * @param cl The class to process.
     * @return The (possibly null) annotation.
     */
    public static <T extends Annotation> T getAnnotation(Class<T> ann, Class<?> cl)
    {
        T res = null;

        res = cl.getAnnotation(ann);
        if (res == null)
        {
            Class<?>[] inter = cl.getInterfaces();
            for (int i = 0; i < inter.length && ann == null; i++)
                res = inter[i].getAnnotation(ann);
        }

        return res;
    }

    // ===========================================================================
    /**
     * Looks for the annotation specified on the given object. The call is dispatched
     * to all the interfaces and superclasses until found, or until Object has been
     * reached.
     *
     * If no such annotation could be found, null is returned.
     *
     * @param cl The annotation class to look for.
     * @param o The object to parse.
     * @return The (possibly null) annotation instance.
     */
    public static <T extends Annotation> T getAnnotationRecursively(Class<T> cl, Object o)
    {
        T ann = null;

        Class<?> toInspect = o.getClass();
        while (toInspect != null && ann == null)
        {
            ann = getAnnotation(cl, toInspect);
            toInspect = toInspect.getSuperclass();
        }

        return ann;
    }

    // ===========================================================================
    /**
     * Returns the parameter annotation requested.
     *
     * @param ann The annotation to look for.
     * @param o The object.
     * @param m The method being inspected.
     * @param param The index of the parameter to process.
     * @return The (possibly null) annotation.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotations(Class<T> ann, Object o,  Method m, int param)
    {
        T res = null;

        Annotation[][] anns = m.getParameterAnnotations();
        for (int i = 0; i < anns[param].length && res == null; i++)
            if (anns[param][i].annotationType().equals(ann)) res = (T) anns[param][i];

        if (res == null)
        {
            Class<?> cl = o.getClass().getSuperclass();
            while (cl != null && res == null)
            {
                try
                {
                    Method m2 = cl.getMethod(m.getName(), m.getParameterTypes());
                    anns = m2.getParameterAnnotations();
                    for (int i = 0; i < anns[param].length && res == null; i++)
                        if (anns[param][i].annotationType().equals(ann)) res = (T) anns[param][i];
                }
                catch (Throwable e)
                {
                    // Ignore it, as this is bound to happen.
                }

                Class<?>[] interf = cl.getInterfaces();
                for (int i = 0; i < interf.length && res == null; i++)
                {
                    try
                    {
                        Method m2 = interf[i].getMethod(m.getName(), m.getParameterTypes());
                        anns = m2.getParameterAnnotations();
                        for (int j = 0; j < anns[param].length && res == null; j++)
                            if (anns[param][j].annotationType().equals(ann)) res = (T) anns[param][j];
                    }
                    catch (Throwable e)
                    {
                        // Ignore it, as this is bound to happen.
                    }
                }
                cl = cl.getSuperclass();
            }
        }

        return res;
    }

    // ===========================================================================
    /**
     * Looks for the annotation given in the method specified. If the annotation is
     * not found on the object, all the superclasses and interfaces are parsed as well.
     *
     * @param ann The annotation to look for.
     * @param m The method being inspected.
     * @param o The object owning the method.
     * @return The (possibly null) annotation.
     */
    public static <T extends Annotation> T getAnnotation(Class<T> ann, Method m, Object o)
    {
        T res = null;

        res = m.getAnnotation(ann);
        if (res == null)
        {
            Class<?> cl = o.getClass().getSuperclass();
            while (cl != null && res == null)
            {
                try
                {
                    Method m2 = cl.getMethod(m.getName(), m.getParameterTypes());
                    res = m2.getAnnotation(ann);
                }
                catch (Throwable e)
                {
                    // Ignore it, as this is bound to happen.
                }

                Class<?>[] interf = cl.getInterfaces();
                for (int i = 0; i < interf.length && res == null; i++)
                {
                    try
                    {
                        Method m2 = interf[i].getMethod(m.getName(), m.getParameterTypes());
                        res = m2.getAnnotation(ann);
                    }
                    catch (Throwable e)
                    {
                        // Ignore it, as this is bound to happen.
                    }
                }
                cl = cl.getSuperclass();
            }
        }

        return res;
    }
}
