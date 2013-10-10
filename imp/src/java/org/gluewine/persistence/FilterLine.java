package org.gluewine.persistence;

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
