/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gluewine.core.Glue;
import org.gluewine.core.glue.Gluer;
import org.gluewine.persistence.PersistenceException;
import org.gluewine.persistence.SessionProvider;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.TransactionalSession;

/**
 * Default implementation of SessionProvider.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SessionProviderImpl implements SessionProvider
{
    // ===========================================================================
    /**
     * The map of sessions indexed on the thread they are running in.
     */
    private Map<Thread, HibernateTransactionalSessionImpl> sessions = new HashMap<Thread, HibernateTransactionalSessionImpl>();

    /**
     * The map of methods having invoked the getSession() when running in non enhanced mode.
     */
    private Map<Thread, Set<String>> methods = new HashMap<Thread, Set<String>>();

    /**
     * The aspect provider to use.
     */
    @Glue
    private SessionAspectProvider provider = null;

    /**
     * The logger to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * We need the gluer to know whether the framework is running in Enhanced mode
     * or not.
     */
    @Glue
    private Gluer gluer = null;

    // ===========================================================================
    @Override
    public TransactionalSession getSession()
    {
        return getSession(null);
    }

    // ===========================================================================
    /**
     * Returns the name of the method that invoked the getSession(), commitSession()
     * or rollbackSession().
     *
     * @return The method name.
     */
    String getMethodName()
    {
        StackTraceElement[] els = new Throwable().getStackTrace();
        boolean found = false;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < els.length && !found; i++)
        {
            if (els[i].getClassName().startsWith(SessionProviderImpl.class.getName())) continue;
            else
            {
                b.append(els[i].getClassName()).append(".").append(els[i].getMethodName());
                b.append("(").append(")");
                found = true;
            }
        }

        return b.toString();
    }

    // ===========================================================================
    @Override
    public TransactionalSession getSession(TransactionCallback callback)
    {
        if (gluer.isEnhancedMode())
        {
            HibernateTransactionalSessionImpl session = sessions.get(Thread.currentThread());
            if (session == null)
            {
                logger.error("No session bound with the current context!");
                throw new PersistenceException("No session bound with current context! Check @Transactional annotation.");
            }
            if (callback != null) session.pushCallback(callback);
            return session;
        }
        else
        {
            Set<String> set = methods.get(Thread.currentThread());
            if (set == null)
            {
                set = new HashSet<String>();
                methods.put(Thread.currentThread(), set);
            }
            String method = getMethodName();

            // In case someone uses getSession() several time in the same method we must ensure that
            // the reference count is not increased.
            provider.before(!set.contains(method));
            set.add(method);

            return getBoundSession();
        }
    }

    // ===========================================================================
    /**
     * Returns the session bound with the current thread context, or null
     * if no session has yet been bound.
     *
     * @return The session bound.
     */
    HibernateTransactionalSessionImpl getBoundSession()
    {
        return sessions.get(Thread.currentThread());
    }

    // ===========================================================================
    /**
     * Binds the given session with the current Thread context.
     *
     * @param session The session to bind.
     */
    void bindSession(HibernateTransactionalSessionImpl session)
    {
        sessions.put(Thread.currentThread(), session);
    }

    // ===========================================================================
    /**
     * Unbinds the session from the current context and returns it.
     */
    void unbindSession()
    {
        sessions.remove(Thread.currentThread());
    }

    // ===========================================================================
    @Override
    public void commitCurrentSession()
    {
        if (!gluer.isEnhancedMode())
        {
            Set<String> stack = methods.get(Thread.currentThread());
            if (stack != null)
            {
                String method = getMethodName();
                stack.remove(method);
                if (stack.isEmpty())
                {
                    methods.remove(Thread.currentThread());
                    provider.afterSuccess();
                }
            }
        }
    }

    // ===========================================================================
    @Override
    public void rollbackCurrentSession()
    {
        if (!gluer.isEnhancedMode())
        {
            Set<String> stack = methods.get(Thread.currentThread());
            if (stack != null)
            {
                String method = getMethodName();
                stack.remove(method);
                if (stack.isEmpty())
                {
                    methods.remove(Thread.currentThread());
                    provider.afterFailure();
                }
            }
        }
    }
}
