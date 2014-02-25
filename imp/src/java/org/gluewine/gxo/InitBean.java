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
 * Bean used to transport an instantiation request.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class InitBean extends GxoBean
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 2365502493246725030L;

    /**
     * The name of the interface or class to instantiate.
     */
    private String className = null;

    /**
     * The constructor parameters to use.
     */
    private Class<?>[] paramTypes = null;

    /**
     * The parameters.
     */
    private Object[] paramValues = null;

    // ===========================================================================
    /**
     * Returns the name of the class.
     *
     * @return The classname.
     */
    public String getClassName()
    {
        return className;
    }

    // ===========================================================================
    /**
     * Sets the name of the class.
     *
     * @param className The name of the class.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    // ===========================================================================
    /**
     * Returns the constructor types to use.
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
     * Sets the constructor types.
     *
     * @param paramTypes The types.
     */
    public void setParamTypes(Class<?>[] paramTypes)
    {
        this.paramTypes = paramTypes;
    }

    // ===========================================================================
    /**
     * Returns the values to use during instantiation.
     *
     * @return The object values.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public Object[] getParamValues()
    {
        return paramValues;
    }

    // ===========================================================================
    /**
     * Sets the values to be used.
     *
     * @param paramValues The object values.
     */
    public void setParamValues(Object[] paramValues)
    {
        this.paramValues = paramValues;
    }
}
