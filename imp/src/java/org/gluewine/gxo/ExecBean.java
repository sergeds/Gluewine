/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
