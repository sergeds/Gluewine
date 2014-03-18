/**************************************************************************
 *
 * Gluewine Console Module
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
package org.gluewine.console;


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
     * Returns the number of arguments present.
     *
     * @return The number of arguments.
     */
    int getArgumentCount();

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
     * Creates a separator row.
     */
    void tableSeparator();

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
