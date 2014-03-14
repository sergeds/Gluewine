/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence_jpa;

import java.io.Serializable;

/**
 * Defines a filter line.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class FilterLine implements Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 1907058174528815171L;

    /**
     * The name of the field this filter line applies to.
     */
    private String fieldName = null;

    /**
     * The operator this line applies to.
     */
    private FilterOperator operator = null;

    /**
     * The value to be checked.
     */
    private Serializable value = null;

    // ===========================================================================
    /**
     * @return the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    // ===========================================================================
    /**
     * @param fieldName the fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    // ===========================================================================
    /**
     * @return the operator.
     */
    public FilterOperator getOperator()
    {
        return operator;
    }

    // ===========================================================================
    /**
     * @param operator the operator to set.
     */
    public void setOperator(FilterOperator operator)
    {
        this.operator = operator;
    }

    // ===========================================================================
    /**
     * @return the value.
     */
    public Serializable getValue()
    {
        return value;
    }

    // ===========================================================================
    /**
     * @param value the value to set.
     */
    public void setValue(Serializable value)
    {
        this.value = value;
    }
}
