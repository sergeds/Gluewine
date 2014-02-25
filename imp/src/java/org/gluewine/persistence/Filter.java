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
package org.gluewine.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a filter that can be applied to persistence calls.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Filter implements Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -905507219717718623L;

    /**
     * The list of filter lines.
     */
    private List<FilterLine> lines = new ArrayList<FilterLine>();

    /**
     * The list of lines to sort on.
     */
    private List<SortLine> sortLines = new ArrayList<SortLine>();

    /**
     * The limit of records to be fetched. (0 means no limit).
     */
    private int limit = 0;

    /**
     * The offset to start from.
     */
    private int offset = 0;

    // ===========================================================================
    /**
     * Returns the lines contained in this filter.
     *
     * @return The list of filter lines.
     */
    public List<FilterLine> getLines()
    {
        List<FilterLine> l = new ArrayList<FilterLine>(lines.size());
        l.addAll(lines);
        return l;
    }

    // ===========================================================================
    /**
     * Returns the list of sort lines.
     *
     * @return The list of sort lines.
     */
    public List<SortLine> getSortLines()
    {
        List<SortLine> l = new ArrayList<SortLine>(sortLines.size());
        l.addAll(sortLines);
        return l;
    }

    // ===========================================================================
    /**
     * Adds a filter line.
     *
     * @param line The line to add.
     */
    public void addFilterLine(FilterLine line)
    {
        lines.add(line);
    }

    // ===========================================================================
    /**
     * Adds a sort line.
     *
     * @param line The line to sort.
     */
    public void addSortLine(SortLine line)
    {
        sortLines.add(line);
    }

    // ===========================================================================
    /**
     * Clears the filter lines.
     */
    public void cleanFilterLines()
    {
        lines.clear();
    }

    // ===========================================================================
    /**
     * @return the limit.
     */
    public int getLimit()
    {
        return limit;
    }

    // ===========================================================================
    /**
     * @param limit the limit to set.
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    // ===========================================================================
    /**
     * @return the offset.
     */
    public int getOffset()
    {
        return offset;
    }

    // ===========================================================================
    /**
     * @param offset the offset to set.
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }
}
