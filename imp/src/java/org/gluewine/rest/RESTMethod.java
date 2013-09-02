package org.gluewine.rest;

import java.lang.reflect.Method;

/**
 * Simple bean that keeps track of a REST annotated method,
 * and the object it belongs to.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class RESTMethod
{
    // ===========================================================================
    /**
     * The annotated method.
     */
    private Method method = null;

    /**
     * The owner of the method.
     */
    private Object object = null;

    // ===========================================================================
    /**
     * @return the method.
     */
    public Method getMethod()
    {
        return method;
    }

    // ===========================================================================
    /**
     * @param method the method to set.
     */
    public void setMethod(Method method)
    {
        this.method = method;
    }

    // ===========================================================================
    /**
     * @return the object.
     */
    public Object getObject()
    {
        return object;
    }

    // ===========================================================================
    /**
     * @param object the object to set.
     */
    public void setObject(Object object)
    {
        this.object = object;
    }
}
