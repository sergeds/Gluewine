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
package org.gluewine.persistence_jpa_hibernate.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Stack;

import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence_jpa.QueryPostProcessor;
import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.gluewine.persistence_jpa_hibernate.HibernateTransactionalSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Provider that can be used for testing purposes.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class TestSessionProvider implements HibernateSessionProvider
{
    // ===========================================================================
    /**
     * The factory to use.
     */
    private SessionFactory factory = null;

    /**
     * The transaction session to use.
     */
    private HibernateTransactionalSession transactionalSession = null;

    /**
     * The transaction in use.
     */
    private Transaction transaction = null;

    /**
     * Flag indicating that another instance is already in use.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_PKGPROTECT")
    protected static boolean open = false;

    /**
     * Sequence number for empty databases.
     */
    private static int dbseq = 0;

    /**
     * The stack of registered callbacks.
     */
    private Stack<TransactionCallback> callbacks = new Stack<TransactionCallback>();

    /**
     * The actual session.
     */
    private Session hibernateSession = null;

    /**
     * The current configuration.
     */
    private Configuration configuration = null;

    // ===========================================================================
    /**
     * Creates an instance. Only to be used by subclasses.
     */
    protected TestSessionProvider()
    {
    }

    /** Sets the open flag.
     * @param open the new open value.
     */
    protected static void setOpen(boolean open)
    {
        TestSessionProvider.open = open;
    }

    /** Gets a sequence number for database files.
     * @return the sequence number.
     */
    private static int getDbSeq()
    {
        return dbseq++;
    }

    // ===========================================================================
    /** Configures the session provider.
     *
     * @param props The config to use.
     * @param classes The annotated classes to include in the Hibernate config.
     */
    protected synchronized void configure(Properties props, Class<?> ... classes)
    {
        configuration = new Configuration();
        configuration.setProperties(props);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();

        for (Class<?> cl : classes)
            configuration.addAnnotatedClass(cl);

        factory = configuration.buildSessionFactory(serviceRegistry);
    }

    // ===========================================================================
    /**
     * Creates an instance. The config file given must point to the file containing
     * the hibernate test configuration.
     *
     * All classes that will need to be used by Hibernate during the test should be
     * specified.
     *
     * @param config The config to use.
     * @param classes The annotated classes to include in the Hibernate config.
     */
    public TestSessionProvider(File config, Class<?> ... classes)
    {
        if (open)
            closeProvider();

        if (config.exists())
        {
            try
            (
                FileInputStream fis = new FileInputStream(config);
            )
            {
                Properties props = new Properties();
                props.load(fis);
                props.setProperty("hibernate.connection.url", props.getProperty("hibernate.connection.url").replaceAll("mem:empty", "mem:empty" + getDbSeq()));

                configure(props, classes);

                setOpen(true);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        else
            throw new RuntimeException("The config " + config.getAbsolutePath() + " does not exist.");
    }
    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public synchronized HibernateTransactionalSession getSession()
    {
        return getSession(null);
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public synchronized HibernateTransactionalSession getSession(TransactionCallback callback)
    {
        if (transaction == null)
        {
            if (transactionalSession == null)
            {
                hibernateSession = factory.openSession();
                transactionalSession = new HibernateTransactionalSessionImpl(hibernateSession, new HashSet<QueryPreProcessor>(), new HashSet<QueryPostProcessor>());
            }

            hibernateSession.clear();
            transaction = hibernateSession.beginTransaction();
        }

        if (callback != null)
            callbacks.push(callback);

        return transactionalSession;
    }

    // ===========================================================================
    /**
     * Closes the provider.
     */
    public synchronized void closeProvider()
    {
        if (transactionalSession != null)
        {
            hibernateSession.close();
            transactionalSession = null;
        }

        if (factory != null)
        {
            factory.close();
            factory = null;
        }

        setOpen(false);
    }

    // ===========================================================================
    @Override
    protected void finalize()
    {
        if (open)
            closeProvider();
    }

    // ===========================================================================
    @Override
    public void commitCurrentSession()
    {
        if (transaction != null)
        {
            transaction.commit();
            transaction = null;

            while (!callbacks.isEmpty())
                callbacks.pop().transactionCommitted();
        }
        else
            throw new RuntimeException("There is no current transaction to commit.");
    }

    // ===========================================================================
    @Override
    public void rollbackCurrentSession()
    {
        if (transaction != null)
        {
            transaction.rollback();
            transaction = null;

            while (!callbacks.isEmpty())
                callbacks.pop().transactionRolledBack();
        }
        else
            throw new RuntimeException("There is no current transaction to rollback.");
    }

    // ===========================================================================
    @Override
    public synchronized Configuration getConfiguration()
    {
        return configuration;
    }
}
