package org.gluewine.persistence_jpa_hibernate;

import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.TransactionalSession;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

/**
 * Defines a Hibernate transactional session.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface HibernateTransactionalSession extends TransactionalSession
{
    // ===========================================================================
    /**
     * Creates and returns a Criteria for the given entity.
     *
     * @param entity The entity to process.
     * @return The Criteria.
     */
    Criteria createCriteria(Class<?> entity);

    // ===========================================================================
    /**
     * Creates and returns a Criteria for the given entity and filter.
     *
     * @param cl The entity to process.
     * @param filter The filter to apply.
     * @return the created Criteria object
     */
    Criteria createCriteria(Class<?> cl, Filter filter);

    // ===========================================================================
    /**
     * Executes the JDBC worker.
     *
     * @param work The worker to execute.
     */
    void doWork(Work work);

    // ===========================================================================
    /**
     * Creates a query object and returns it. Pre- and Post processors will not
     * be notified.
     *
     * @param query The query string.
     * @return the created Query object
     */
    Query createQuery(String query);

    // ===========================================================================
    /**
     * Returns the delegate session.
     *
     * @return The delegate.
     */
    Session getDelegate();
}
