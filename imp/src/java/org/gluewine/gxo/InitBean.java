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
