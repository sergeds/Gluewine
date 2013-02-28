/**************************************************************************
 *
 * Gluewine Console Module
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
package org.gluewine.console;

import java.util.Map;

/**
 * Defines the CommandContext.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface CommandContext
{
    // ===========================================================================
    /**
     * Returns true if the current context has the given option.
     *
     * @param option The option to check for.
     * @return True if the option is present.
     */
    boolean hasOption(String option);

    // ===========================================================================
    /**
     * Returns the array of parsed options.
     *
     * @return The array of options.
     */
    String[] getOptions();

    // ===========================================================================
    /**
     * Parses the options specified, and throws a Runtime exception if
     * the CLI arguments don't match the given option rules.
     *
     *  <p>The map has the following format:
     *  <ul>
     *  <li>key: the option</li>
     *  <li>value: an array of boolean where:
     *  <ul>
     *  <li>element 0: whether the option is required</li>
     *  <li>element 1: whether the option requires a value</li>
     *  </ul>
     *  </li>
     *  </ul>
     *
     * @param options The options to parse.
     * @param syntax The String representing the correct syntax.
     * @throws Throwable If a required option is missing or when an option has no value when needed.
     */
    void parseOptions(Map<String, boolean[]> options, String syntax) throws Throwable;

    // ===========================================================================
    /**
     * Returns the parsed value for the given option, or null if there's no such
     * option.
     *
     * @param option The option to process.
     * @return The (possibly null) parsed value.
     */
    String getOption(String option);

    // ===========================================================================
    /**
     * Prints out a String followed by a line feed.
     *
     * @param s The String to output.
     */
    void println(String s);

    // ===========================================================================
    /**
     * Prints all given Strings. A blank space will be outputted between every
     * String.
     *
     * @param s The Strings to output.
     */
    void println(String ... s);

    // ===========================================================================
    /**
     * Outputs the stacktrace of the given Throwable.
     *
     * @param e The Throwable to output.
     */
    void printStackTrace(Throwable e);

    // ===========================================================================
    /**
     * Prints a new line.
     */
    void println();

    // ===========================================================================
    /**
     * Returns the next argument, or null if no more arguments are available.
     *
     * @return The (possibly null) arguement.
     */
    String nextArgument();

    // ===========================================================================
    /**
     * Creates a new table with the given header.
     *
     * @param s The column headers.
     */
    void tableHeader(String ... s);

    // ===========================================================================
    /**
     * Sets the max widths of the columns. 0 indicates no maximum restriction.
     *
     * @param w The widths.
     */
    void tableMaxColumnWidth(int ... w);

    // ===========================================================================
    /**
     * Adds a row to the current table. If no table is available a new one is
     * created (without a header).
     *
     * @param s The row fields.
     */
    void tableRow(String ... s);

    // ===========================================================================
    /**
     * Prints out the current table, and discards it.
     */
    void printTable();
}
