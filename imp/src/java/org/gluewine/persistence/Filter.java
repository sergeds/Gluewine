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
     * Clears the filter lines.
     */
    public void cleanFilterLines()
    {
        lines.clear();
    }
}
