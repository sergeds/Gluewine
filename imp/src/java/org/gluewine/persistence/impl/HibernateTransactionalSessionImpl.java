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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.gluewine.persistence.QueryPostProcessor;
import org.gluewine.persistence.QueryPreProcessor;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.TransactionalSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;

/**
 * Deletegator for the Hibernate sessions.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class HibernateTransactionalSessionImpl implements TransactionalSession
{
    // ===========================================================================
    /**
     * The session to delegate to.
     */
    private Session delegate = null;

    /**
     * The registered PostProcessors.
     */
    private Set<QueryPostProcessor> postProcessors = new HashSet<QueryPostProcessor>();

    /**
     * The registered PreProcessors.
     */
    private Set<QueryPreProcessor> preProcessors = new HashSet<QueryPreProcessor>();

    /**
     * The LIFO stack of registered callback objects.
     */
    private Stack<TransactionCallback> callbacks = new Stack<TransactionCallback>();

    /**
     * The reference counter.
     */
    private int referenceCount = 0;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param session The session to delegate to.
     * @param preProcessors The set of registered preprocessors.
     * @param postProcessors The set of registered postprocessors.
     */
    HibernateTransactionalSessionImpl(Session session, Set<QueryPreProcessor> preProcessors, Set<QueryPostProcessor> postProcessors)
    {
        this.delegate = session;
        this.preProcessors.addAll(preProcessors);
        this.postProcessors.addAll(postProcessors);
    }

    // ===========================================================================
    /**
     * Increases the reference count by 1.
     */
    void increaseReferenceCount()
    {
        referenceCount++;
    }

    // ===========================================================================
    /**
     * Decreases the reference count by 1.
     */
    void decreaseReferenceCount()
    {
        referenceCount--;
    }

    // ===========================================================================
    /**
     * Returns the reference count.
     *
     * @return The reference count.
     */
    int getReferenceCount()
    {
        return referenceCount;
    }

    // ===========================================================================
    /**
     * Returns the hibernate session.
     *
     * @return The hibernate session.
     */
    Session getHibernateSession()
    {
        return delegate;
    }

    // ===========================================================================
    /**
     * Pushes a callback object in the stack of registered callbacks.
     *
     * @param callback The callback to push.
     */
    void pushCallback(TransactionCallback callback)
    {
        callbacks.push(callback);
    }

    // ===========================================================================
    /**
     * Returns the stack of registered callbacks.
     *
     * @return The stack of callbacks.
     */
    Stack<TransactionCallback> getRegisteredCallbacks()
    {
        return callbacks;
    }

    // ===========================================================================
    @Override
    public Serializable add(Object o)
    {
        Serializable id = delegate.save(o);

        for (QueryPostProcessor post : postProcessors)
            post.added(id, o);

        return id;
    }

    // ===========================================================================
    @Override
    public void addOrUpdate(Object o)
    {
        delegate.saveOrUpdate(o);

        Serializable id = delegate.getIdentifier(o);
        for (QueryPostProcessor post : postProcessors)
            post.addedOrUpdated(id, o);
    }

    // ===========================================================================
    @Override
    public Criteria createCriteria(Class<?> entity)
    {
        Criteria cr = delegate.createCriteria(entity);
        cr.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        for (QueryPreProcessor pre : preProcessors)
            cr = pre.preProcess(cr, entity);

        return cr;
    }

    // ===========================================================================
    @Override
    public void delete(Object o)
    {
        Serializable id = delegate.getIdentifier(o);
        delegate.delete(o);
        for (QueryPostProcessor post : postProcessors)
            post.deleted(id, o);
    }

    // ===========================================================================
    @Override
    public void delete(String type, Object id)
    {
        delegate.delete(type, id);
        for (QueryPostProcessor post : postProcessors)
            post.deleted((Serializable) id, type);
    }

    // ===========================================================================
    @Override
    public void doWork(Work work)
    {
        delegate.doWork(work);
    }

    // ===========================================================================
    @Override
    public Object get(Class<?> cl, Serializable id)
    {
        Criteria cr = createCriteria(cl);
        cr.add(Restrictions.idEq(id));
        return cr.uniqueResult();
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> getAll(Class<E> cl)
    {
        Criteria cr = createCriteria(cl);
        return cr.list();
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> getAllSorted(Class<E> cl, String sortField, boolean ascending)
    {
        Criteria cr = createCriteria(cl);

        if (ascending) cr.addOrder(Property.forName(sortField).asc());
        else cr.addOrder(Property.forName(sortField).desc());

        return cr.list();
    }

    // ===========================================================================
    @Override
    public void update(Object o)
    {
        delegate.update(o);

        Serializable id = delegate.getIdentifier(o);
        for (QueryPostProcessor post : postProcessors)
            post.updated(id, o);
    }
}
