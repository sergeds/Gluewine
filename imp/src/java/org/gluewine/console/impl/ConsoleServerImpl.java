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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gluewine.console.Authenticator;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.console.ConsoleServer;
import org.gluewine.console.SyntaxException;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;

/**
 * Default implementation of ConsoleServer.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ConsoleServerImpl implements ConsoleServer, CommandProvider
{
    // ===========================================================================
    /**
     * Map of providers indexed on the commands they provide.
     */
    private Map<String, CommandProvider> providers = new HashMap<String, CommandProvider>();

    /**
     * The map of authenticator classes indexed on their user friendly name.
     */
    private Map<String, String> authenticators = new HashMap<String, String>();

    @Glue
    private Repository repository = null;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    public ConsoleServerImpl()
    {
        providers.put("_help", this);
    }

    // ===========================================================================
    /**
     * Checks for available authenticators and registers them.
     */
    @RunOnActivate
    public void checkAuthenticators()
    {
        repository.addListener(new RepositoryListener<Authenticator>()
        {
            @Override
            public void registered(Authenticator a)
            {
                authenticators.put(a.getAuthenticatorName(), a.getAuthenticatorClassName());
            }

            @Override
            public void unregistered(Authenticator a)
            {
                authenticators.remove(a.getAuthenticatorName());
            }
        });
    }

    // ===========================================================================
    @Override
    public String executeCommand(String command) throws Throwable
    {
        int i = command.indexOf(' ');
        String params = null;
        if (i > 0)
        {
            params = command.substring(i).trim();
            command = command.substring(0, i);
        }

        if (providers.containsKey(command))
        {
            CommandProvider prov = providers.get(command);
            BufferedCommandInterpreter ci = new BufferedCommandInterpreter(params);

            try
            {
                Method m = prov.getClass().getMethod("_" + command, CommandContext.class);
                m.invoke(prov, new Object[] {ci});
            }
            catch (Throwable e)
            {
                if (e instanceof InvocationTargetException)
                    e = ((InvocationTargetException) e).getCause();

                if (e instanceof SyntaxException)
                {
                    ci.println(e.getMessage());
                }
                else
                    throw e;
            }

            return ci.getOutput();
        }

        return "Unknown command !";
    }

    // ===========================================================================
    @Override
    public void registered(CommandProvider t)
    {
        for (String cmd : indexCommands(t))
            providers.put(cmd, t);
    }

    // ===========================================================================
    @Override
    public void unregistered(CommandProvider t)
    {
        for (String cmd : indexCommands(t))
            providers.remove(cmd);
    }

    // ===========================================================================
    /**
     * Indexes the given CommandProvider and returns the list of commands it
     * provides.
     *
     * @param c The CommandProvider to index.
     * @return The list of commands.
     */
    private List<String> indexCommands(CommandProvider c)
    {
        List<String> l = new ArrayList<String>();

        Method[] methods = c.getClass().getMethods();
        for (Method method : methods)
        {
            if (method.getName().startsWith("_"))
            {
                Class<?>[] params = method.getParameterTypes();
                if (params != null && params.length == 1 && CommandContext.class.isAssignableFrom(params[0]))
                    l.add(method.getName().substring(1));
            }
        }

        return l;
    }

    // ===========================================================================
    /**
     * Executes the help command.
     *
     * @param ci The current context.
     */
    public void _help(CommandContext ci)
    {
        Map<String, String> m = new TreeMap<String, String>();

        int max = 0;
        for (CommandProvider p : providers.values())
        {
            for (Entry<String, String> e : p.getCommandsSyntax().entrySet())
            {
                max = Math.max(max, e.getKey().length());
                m.put(e.getKey(), e.getValue());
            }
        }

        ci.tableHeader("Command", "Description");

        String prevLetter = null;
        for (Entry<String, String> e : m.entrySet())
        {
            String firstLetter = e.getKey().substring(0, 1);
            if (prevLetter != null && !prevLetter.equals(firstLetter))
                ci.tableRow("@@-@@", "@@-@@");

            ci.tableRow(e.getKey(), e.getValue());
            prevLetter = firstLetter;
        }

        ci.printTable();
    }

    // ===========================================================================
    /**
     * Executes the h command, which is a shortcut for help.
     *
     * @param ci The current context.
     */
    public void _h(CommandContext ci)
    {
        _help(ci);
    }

    // ===========================================================================
    @Override
    public boolean needsAuthentication()
    {
        return !authenticators.isEmpty();
    }

    // ===========================================================================
    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();

        m.put("help | h", "Displays the list of available commands.");

        return m;
    }

    // ===========================================================================
    @Override
    public List<String> complete(String command)
    {
        List<String> res = new ArrayList<String>();

        String[] cmd = null;
        if (command == null || command.trim().length() == 0) cmd = new String[] {""};
        else cmd = command.split(" ");

        for (String s : providers.keySet())
        {
            if (s.startsWith(cmd[0]))
                res.add(s);
        }

        Collections.sort(res);
        return res;
    }

    // ===========================================================================
    @Override
    public Map<String, String> getAvailableAuthenticators()
    {
        Map<String, String> m = new HashMap<String, String>(authenticators.size());
        m.putAll(authenticators);
        return m;
    }
}
