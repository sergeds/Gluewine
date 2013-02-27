/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;


/**
 * Bean used to transport a method invocation request.
 *
 * It contains the service name. (unqualified), the
 * parameters and parameter types.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ExecBean extends GxoBean
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 5746967009923303285L;

    /**
     * The unqualified service name.
     */
    private String service;

    /**
     * The name of the method to be invoked.
     */
    private String method;

    /**
     * The method parameters.
     */
    private Object[] params = new Object[0];

    /**
     * The parameter types.
     */
    private Class<?>[] paramTypes = new Class<?>[0];

    /**
     * The current sessionId.
     */
    private String sessionId = null;

    /**
     * The address of the invoker.
     */
    private String ipAddress = null;

    // ===========================================================================
    /**
     * Returns the array of parameter types.
     *
     * @return The types.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public Class<?>[] getParamTypes()
    {
        return paramTypes;
    }

    // ===========================================================================
    /**
     * Sets the types.
     *
     * @param paramTypes The types.
     */
    public void setParamTypes(Class<?>[] paramTypes)
    {
        this.paramTypes = paramTypes;
    }

    // ===========================================================================
    /**
     * Returns the service name.
     *
     * @return The name of the service.
     */
    public String getService()
    {
        return service;
    }

    // ===========================================================================
    /**
     * Sets the service name.
     *
     * @param service The service name.
     */
    public void setService(String service)
    {
        this.service = service;
    }

    // ===========================================================================
    /**
     * Returns the method name.
     *
     * @return The method name.
     */
    public String getMethod()
    {
        return method;
    }

    // ===========================================================================
    /**
     * Sets the method name.
     *
     * @param method The name of the method to invoke.
     */
    public void setMethod(String method)
    {
        this.method = method;
    }

    // ===========================================================================
    /**
     * Returns the parameters.
     *
     * @return The parameters.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public Object[] getParams()
    {
        return params;
    }

    // ===========================================================================
    /**
     * Sets the array of parameters.
     *
     * @param params The parameters.
     */
    public void setParams(Object[] params)
    {
        this.params = params;
    }

    // ===========================================================================
    /**
     * Returns the session id.
     *
     * @return The current sessionid.
     */
    public String getSessionId()
    {
        return sessionId;
    }

    // ===========================================================================
    /**
     * Sets the session id.
     *
     * @param sessionId The session id.
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    // ===========================================================================
    /**
     * Returns the ip address of the invoker.
     *
     * @return The ip address.
     */
    public String getIpAddress()
    {
        return ipAddress;
    }

    // ===========================================================================
    /**
     * Sets the ip address of the invoker.
     *
     * @param ipAddress The ip address.
     */
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }
}
