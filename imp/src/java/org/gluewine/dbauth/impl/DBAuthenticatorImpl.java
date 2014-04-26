/**************************************************************************
 *
 * Gluewine Database Authentication Module
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
package org.gluewine.dbauth.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gluewine.authentication.AuthenticationException;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CLIOption;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.RunOnActivate;
import org.gluewine.dbauth.DBAuthenticator;
import org.gluewine.dbauth.DBCredential;
import org.gluewine.persistence.PersistenceException;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa.SessionProvider;
import org.gluewine.sessions.SessionManager;
import org.gluewine.sessions.Unsecured;

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

    /**
     * The logger to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Checks that there's at least 1 credential defined. If not, it will create a
     * default one.
     *
     * @throws AuthenticationException If an error occurs setting the initial credential.
     */
    @RunOnActivate
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
    @Unsecured
    public String authenticate(String user, String password) throws AuthenticationException
    {
        try
        {
            DBCredential cred = (DBCredential) provider.getSession().get(DBCredential.class, user);
            if (cred != null)
            {
                if (logger.isDebugEnabled()) logger.debug("Found credentials for user " + user);
                if (cred.verify(password))
                {
                    if (logger.isDebugEnabled()) logger.debug("Password verified successfully for user " + user);
                    String session = sessionManager.createNewSession(user);
                    if (logger.isDebugEnabled()) logger.debug("Obtained new session " + session + " for user " + user);
                    return session;
                }
                else
                {
                    if (logger.isDebugEnabled()) logger.debug("Password did not verified for user " + user);
                    throw new AuthenticationException("Invalid user or password");
                }
            }
            else
            {
                if (logger.isDebugEnabled()) logger.debug("No credentials found for user " + user);
                throw new AuthenticationException("Invalid user or password");
            }
        }
        catch (PersistenceException e)
        {
            throw new AuthenticationException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    @Transactional
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
        ci.tableHeader("Users");
        for (DBCredential dbc : l)
            ci.tableRow(dbc.getUserid());

        ci.printTable();
    }

    // ===========================================================================
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();

        CLICommand cmd = null;

        cmd = new CLICommand("dbauth_add", "Adds a password to a user.");
        cmd.addOption(new CLIOption("-user", "userid", true, true));
        cmd.addOption(new CLIOption("-pw", "password", true, true, '\0'));
        commands.add(cmd);

        cmd = new CLICommand("dbauth_set", "Sets the password of a user.");
        cmd.addOption(new CLIOption("-user", "userid", true, true));
        cmd.addOption(new CLIOption("-pw", "password", true, true, '\0'));
        commands.add(cmd);

        cmd = new CLICommand("dbauth_del", "Sets the password of a user.");
        cmd.addOption(new CLIOption("-user", "userid", true, true));
        commands.add(cmd);

        commands.add(new CLICommand("dbauth_list", "Lists all available users."));

        return commands;
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
