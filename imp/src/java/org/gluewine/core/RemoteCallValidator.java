package org.gluewine.core;

import java.lang.reflect.Method;

/**
 * RemoteCallValidator is used to implement RPC security policies.
 *
 * @author fks/Frank Gevaerts
 */
public interface RemoteCallValidator
{
    /**
     * Checks if a method call should be allowed.
     * @param entrypoint The way the method is called (GWT, GXO, ...)
     * @param o The object the method will be invoked on.
     * @param m The method that is going to be invoked.
     * @param params The parameters of the method.
     *
     * @throws Exception if the method call is not allowed.
     */
    void validateCall(String entrypoint, Object o, Method m, Object[] params) throws Exception;
}

