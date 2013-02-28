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

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param params The command parameters.
     */
    BufferedCommandInterpreter(String params)
    {
        this.parameters = params;
        if (parameters != null)  split = parameters.split(" ");
        else
        {
            parameters = "";
            split = new String[0];
        }
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
    @Override
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
}
