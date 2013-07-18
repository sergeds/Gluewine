package org.gluewine.persistence.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Stack;

import org.gluewine.persistence.QueryPostProcessor;
import org.gluewine.persistence.QueryPreProcessor;
import org.gluewine.persistence.SessionProvider;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.TransactionalSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Provider that can be used for testing purposes.
 *
 * @author Serge de Schaetzen
 *
 */
public class TestSessionProvider implements SessionProvider
{
    // ===========================================================================
    /**
     * The factory to use.
     */
    private SessionFactory factory = null;

    /**
     * The transaction session to use.
     */
    private TransactionalSession transactionalSession = null;

    /**
     * The transaction in use.
     */
    private Transaction transaction = null;

    /**
     * Flag indicating that another instance is already in use.
     */
    protected static boolean open = false;

    /**
     * Sequence number for empty databases
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

    // ===========================================================================
    /** Configures the session provider.
     *
     * @param props The config to use.
     * @param classes The annotated classes to include in the Hibernate config.
     */
    protected void configure(Properties props, Class<?> ... classes)
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
            {
                Properties props = new Properties();
                props.load(new FileInputStream(config));
                props.setProperty("hibernate.connection.url",props.getProperty("hibernate.connection.url").replaceAll("mem:empty","mem:empty" + (dbseq++)));

                configure(props, classes);

                open = true;
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
    public synchronized TransactionalSession getSession()
    {
        return getSession(null);
    }

    // ===========================================================================
    @Override
    public synchronized TransactionalSession getSession(TransactionCallback callback)
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
    public void closeProvider()
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

        open = false;
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

            while (! callbacks.isEmpty())
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

            while (! callbacks.isEmpty())
                callbacks.pop().transactionRolledBack();
        }
        else
            throw new RuntimeException("There is no current transaction to rollback.");
    }
}
