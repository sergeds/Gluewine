/**************************************************************************
 *
 * Gluewine Database Authentication Module
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
package org.gluewine.dbauth.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluewine.console.AuthenticationException;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.console.ConsoleServer;
import org.gluewine.core.Glue;
import org.gluewine.core.RunWhenGlued;
import org.gluewine.dbauth.DBAuthenticator;
import org.gluewine.persistence.PersistenceException;
import org.gluewine.persistence.SessionProvider;
import org.gluewine.persistence.Transactional;
import org.gluewine.sessions.SessionManager;

/**
 * Default implementation of DBAuthenticator.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DBAuthenticatorImpl implements DBAuthenticator, CommandProvider
{
    // ===========================================================================
    /**
     * The session provider to use.
     */
    @Glue
    private SessionProvider provider = null;

    /**
     * The session manager to use.
     */
    @Glue
    private SessionManager sessionManager = null;

    @Glue
    private ConsoleServer server = null;

    // ===========================================================================
    /**
     * Checks that there's at least 1 credential defined. If not, it will create a
     * default one.
     *
     * @throws AuthenticationException If an error occurs setting the initial credential.
     */
    @RunWhenGlued
    @Transactional
    public void checkInitialUser() throws AuthenticationException
    {
        try
        {
            List<DBCredential> l = provider.getSession().getAll(DBCredential.class);
            if (l == null || l.isEmpty())
                addCredential("manager", "manager");

            provider.commitCurrentSession();
        }
        catch (PersistenceException e)
        {
            provider.rollbackCurrentSession();
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    @Transactional
    public String authenticate(String user, String password) throws AuthenticationException
    {
        try
        {
            DBCredential cred = (DBCredential) provider.getSession().get(DBCredential.class, user);
            provider.commitCurrentSession();
            if (cred != null)
            {
                if (cred.verify(password)) return sessionManager.createNewSession();
                else throw new AuthenticationException("Invalid user or password");
            }
            else throw new AuthenticationException("Invalid user or password");
        }
        catch (PersistenceException e)
        {
            provider.rollbackCurrentSession();
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    public void resetPassword(String user, String password) throws AuthenticationException
    {
        try
        {
            DBCredential cred = (DBCredential) provider.getSession().get(DBCredential.class, user);
            if (cred != null)
            {
                cred.setPassword(password);
                provider.getSession().update(cred);
                provider.commitCurrentSession();
            }
            else
            {
                provider.commitCurrentSession();
                throw new AuthenticationException("User " + user + " does not exist.");
            }
        }
        catch (PersistenceException e)
        {
            provider.rollbackCurrentSession();
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    /**
     * Executes the dbauth_add command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _dbauth_add(CommandContext ci) throws Throwable
    {
        Map<String, boolean[]> opts = new HashMap<String, boolean[]>();
        opts.put("-user", new boolean[] {true, true});
        opts.put("-pw", new boolean[] {true, true});
        ci.parseOptions(opts, "dbauth_add -user <userid> -pw <password>");
        addCredential(ci.getOption("-user"), ci.getOption("-pw"));
    }

    // ===========================================================================
    /**
     * Executes the dbauth_set command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _dbauth_set(CommandContext ci) throws Throwable
    {
        Map<String, boolean[]> opts = new HashMap<String, boolean[]>();
        opts.put("-user", new boolean[] {true, true});
        opts.put("-pw", new boolean[] {true, true});
        ci.parseOptions(opts, "dbauth_set-user <userid> -pw <password>");
        resetPassword(ci.getOption("-user"), ci.getOption("-pw"));
    }

    // ===========================================================================
    /**
     * Executes the dbauth_del command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _dbauth_del(CommandContext ci) throws Throwable
    {
        Map<String, boolean[]> opts = new HashMap<String, boolean[]>();
        opts.put("-user", new boolean[] {true, true});
        ci.parseOptions(opts, "dbauth_del -user <userid> -pw <password>");
        delCredential(ci.getOption("-user"));
    }

    // ===========================================================================
    /**
     * Executes the dbauth_list command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    @Transactional
    public void _dbauth_list(CommandContext ci) throws Throwable
    {
        List<DBCredential> l = provider.getSession().getAll(DBCredential.class);
        ci.println("Users:");
        ci.println("------");
        for (DBCredential dbc : l)
            ci.println(dbc.getUserid());
    }

    // ===========================================================================
    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();

        m.put("dbauth_add", "-user <userid> -pw <password> : Adds a password to a user.");
        m.put("dbauth_set", "-user <userid> -pw <password> : Sets the password of a user.");
        m.put("dbauth_del", "-user <userid> : Deletes the password of a user.");
        m.put("dbauth_list", "Lists all available users.");

        return m;
    }

    // ===========================================================================
    @Override
    @Transactional
    public void addCredential(String user, String password) throws AuthenticationException
    {
        DBCredential dbc = new DBCredential();
        dbc.setUserid(user);
        dbc.setPassword(password);
        try
        {
            provider.getSession().add(dbc);
            provider.commitCurrentSession();
        }
        catch (PersistenceException e)
        {
            provider.rollbackCurrentSession();
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    @Transactional
    public void delCredential(String user) throws AuthenticationException
    {
        try
        {
            DBCredential dbc = (DBCredential) provider.getSession().get(DBCredential.class, user);
            if (dbc != null)
            {
                provider.getSession().delete(dbc);
                provider.commitCurrentSession();
            }
            else
            {
                provider.rollbackCurrentSession();
                throw new AuthenticationException("User " + user + " does not exist.");
            }
        }
        catch (PersistenceException e)
        {
            provider.rollbackCurrentSession();
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    public String getAuthenticatorName()
    {
        return "Database Authentication";
    }

    // ===========================================================================
    @Override
    public String getAuthenticatorClassName()
    {
        return "org.gluewine.dbauth.DBAuthenticator";
    }
}
