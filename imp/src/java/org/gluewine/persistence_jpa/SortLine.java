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
