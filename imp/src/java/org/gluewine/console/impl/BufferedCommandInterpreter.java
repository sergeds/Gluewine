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
package org.gluewine.console.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gluewine.console.CommandContext;
import org.gluewine.console.SyntaxException;

/**
 * A command context that buffers the output.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class BufferedCommandInterpreter implements CommandContext
{
    // ===========================================================================
    /**
     * The buffer.
     */
    private StringBuilder buffer = new StringBuilder();

    /**
     * The parameters String.
     */
    private String parameters = null;

    /**
     * The parameters split.
     */
    private String[] split = null;

    /**
     * The current index.
     */
    private int paramIndex = 0;

    /**
     * The map of parsed options and their value.
     */
    private Map<String, String> options = new HashMap<String, String>();

    /**
     * The linefeed character.
     */
    private static final char LF = '\n';

    /**
     * The current table. (if any).
     */
    private Table table = null;

    /** Whether the output is routed or not. */
    private boolean outputRouted = false;

    /** Whether the command was entered from batch mode. */
    private boolean batch = false;

    /** Whether the option values were entered interactively. */
    private boolean interactive = false;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param params The command parameters.
     * @param outputRouted Whether the output is routed or not.
     * @param batch Whether the command was entered from batch mode.
     * @param interactive Whether the option values were entered interactively.
     */
    BufferedCommandInterpreter(String params, boolean outputRouted, boolean batch, boolean interactive)
    {
        this.parameters = params;
        this.outputRouted = outputRouted;
        this.batch = batch;
        this.interactive = interactive;
        if (parameters != null)  split = parameters.split(" ");
        else
        {
            parameters = "";
            split = new String[0];
        }
    }

    // ===========================================================================
    @Override
    public int getArgumentCount()
    {
        return split.length;
    }

    // ===========================================================================
    @Override
    public boolean hasOption(String option)
    {
        return options.containsKey(option);
    }

    // ===========================================================================
    /**
     * Returns the output containined in the buffer.
     *
     * @return The output.
     */
    String getOutput()
    {
        return buffer.toString();
    }

    // ===========================================================================
    @Override
    public String[] getOptions()
    {
        return options.keySet().toArray(new String[options.size()]);
    }

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
     * @param opts The options to parse.
     * @param syntax The String representing the correct syntax.
     * @throws Throwable If a required option is missing or when an option has no value when needed.
     */
    public void parseOptions(Map<String, boolean[]> opts, String syntax) throws Throwable
    {
        String syn = ", (syntax: " + syntax + ")";

        options = new HashMap<String, String>();
        String remaining = parameters.trim();
        char currentDelimiter = ' ';
        String currentOption = null;
        String currentValue = null;
        int nextTokenBegin = 0;
        boolean required = true;
        do
        {
            nextTokenBegin = remaining.indexOf(currentDelimiter);
            if (nextTokenBegin < 0)
                nextTokenBegin = remaining.length();

            String tokenToCheck = remaining.substring(0, nextTokenBegin);
            if (remaining.startsWith("'") || remaining.startsWith("\""))
            {
                currentDelimiter = remaining.charAt(0);
                nextTokenBegin = remaining.indexOf(currentDelimiter, 1);

                if (nextTokenBegin < 0) throw new SyntaxException("Unclosed quoted string !");

                tokenToCheck = remaining.substring(1, nextTokenBegin);
                currentDelimiter = ' ';
                nextTokenBegin = remaining.indexOf(currentDelimiter, nextTokenBegin + 1);
                if (nextTokenBegin < 0) nextTokenBegin = remaining.length();
            }

            if (opts.containsKey(tokenToCheck))
            {
                // Store previous option
                if (currentOption != null)
                {
                    if (currentValue == null)
                        throw new SyntaxException("Missing value for " + currentOption);

                    options.put(currentOption, currentValue);
                    currentValue = null;
                    currentOption = null;
                }

                boolean[] b = opts.get(tokenToCheck);
                required = b[0];
                if (options.containsKey(tokenToCheck))
                    throw new SyntaxException("Duplicate option " + tokenToCheck);

                if (!b[1])
                    options.put(tokenToCheck, null);

                else
                    currentOption = tokenToCheck;
            }
            else
            {
                if (currentValue == null)
                    currentValue = tokenToCheck;

                else
                    currentValue += " " + tokenToCheck;
            }

            remaining = remaining.substring(nextTokenBegin).trim();
        }
        while (remaining.length() > 0);

        // Store last option
        if (currentOption != null)
            options.put(currentOption, currentValue);

        else if (currentValue != null && currentValue.trim().length() > 0 && required)
            throw new SyntaxException("Unhandled values " + currentValue + syn);

        // Check that we have all required options:
        for (Entry<String, boolean[]> e : opts.entrySet())
        {
            if (e.getValue().length == 2)
            {
                if (e.getValue()[0])
                {
                    if (!options.containsKey(e.getKey()))
                        throw new SyntaxException("Missing required option " + e.getKey() + syn);
                }
            }
            else
                throw new SyntaxException("Invalid boolean array for option " + e.getKey() + syn);
        }
    }

    // ===========================================================================
    @Override
    public String getOption(String option)
    {
        return options.get(option);
    }

    // ===========================================================================
    @Override
    public void println(String s)
    {
        buffer.append(s);
        buffer.append(LF);
    }

    // ===========================================================================
    @Override
    public void println(String... s)
    {
        for (int i = 0; i < s.length; i++)
        {
            buffer.append(s[i]);
            if (i < s.length - 1) buffer.append(" ");
        }
        buffer.append(LF);
    }

    // ===========================================================================
    @Override
    public void printStackTrace(Throwable e)
    {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        buffer.append(result.toString());
        buffer.append(LF);
    }

    // ===========================================================================
    @Override
    public void println()
    {
        buffer.append(LF);
    }

    // ===========================================================================
    @Override
    public String nextArgument()
    {
        if (split != null && paramIndex < split.length) return split[paramIndex++];
        else return null;
    }

    // ===========================================================================
    @Override
    public void tableHeader(String... s)
    {
        if (table == null) table = new Table();
        table.setHeader(s);
    }

    // ===========================================================================
    @Override
    public void tableRow(String... s)
    {
        if (table == null) table = new Table();
        table.addRow(s);
    }

    // ===========================================================================
    @Override
    public void printTable()
    {
        if (table != null)
        {
            table.print(this);
            table = null;
        }
    }

    // ===========================================================================
    @Override
    public void tableMaxColumnWidth(int... w)
    {
        if (table == null) table = new Table();
        table.setMaxWidth(w);
    }

    // ===========================================================================
    @Override
    public void tableSeparator()
    {
        tableRow("@@-@@");
    }

    // ===========================================================================
    /**
     * @return If true, indicates that the output is routed to a file.
     */
    public boolean isOutputRouted()
    {
        return outputRouted;
    }

    // ===========================================================================
    /**
     * @return If true, the command was entered from a batch file.
     */
    public boolean isBatch()
    {
        return batch;
    }

    // ===========================================================================
    /**
     * @return If true, the option values were entered interactively.
     */
    public boolean isInteractive()
    {
        return interactive;
    }
}
