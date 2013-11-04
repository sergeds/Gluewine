package org.gluewine.persistence;

import java.io.Serializable;

/**
 * Defines a sort line.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SortLine implements Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -278508946823872521L;

    // ===========================================================================
    /**
     * The name of the field to sort on.
     */
    private String field = null;

    // ===========================================================================
    /**
     * True to sort ascending, false for descending.
     */
    private boolean ascending = true;

    // ===========================================================================
    /**
     * @return the field
     */
    public String getField()
    {
        return field;
    }

    // ===========================================================================
    /**
     * @param field the field to set
     */
    public void setField(String field)
    {
        this.field = field;
    }

    // ===========================================================================
    /**
     * @return the ascending.
     */
    public boolean isAscending()
    {
        return ascending;
    }

    // ===========================================================================
    /**
     * @param ascending the ascending to set.
     */
    public void setAscending(boolean ascending)
    {
        this.ascending = ascending;
    }
}
