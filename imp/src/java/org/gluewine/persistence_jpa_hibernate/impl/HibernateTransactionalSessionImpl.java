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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.FilterLine;
import org.gluewine.persistence_jpa.QueryPostProcessor;
import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.gluewine.persistence_jpa.SortLine;
import org.gluewine.persistence_jpa_hibernate.HibernateTransactionalSession;
import org.hibernate.Criteria;
import org.hibernate.Query;
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
public class HibernateTransactionalSessionImpl implements HibernateTransactionalSession
{
    // ===========================================================================
    /** The session to delegate to. */
    private Session delegate = null;

    /** The registered PostProcessors. */
    private Set<QueryPostProcessor> postProcessors = new HashSet<QueryPostProcessor>();

    /** The registered PreProcessors. */
    private Set<QueryPreProcessor> preProcessors = new HashSet<QueryPreProcessor>();

    /** The LIFO stack of registered callback objects. */
    private Stack<TransactionCallback> callbacks = new Stack<TransactionCallback>();

    /** The reference counter. */
    private int referenceCount = 0;

    /** Context Counter. Session may only be closed when this counter reaches 0. */
    private int contextCount = 0;

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
     * Increases the context reference count by 1.
     */
    void increaseContextCount()
    {
        contextCount++;
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
     * Decreases the context reference count by 1.
     */
    void decreaseContextCount()
    {
        contextCount--;
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
     * Returns the context reference count.
     *
     * @return The context reference count.
     */
    int getContextCount()
    {
        return contextCount;
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
    public Object merge(Object o)
    {
        Object merged = delegate.merge(o);
        Serializable id = delegate.getIdentifier(o);
        for (QueryPostProcessor post : postProcessors)
            post.updated(id, merged);

        return merged;
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
    public Query createQuery(String query)
    {
        return delegate.createQuery(query);
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
    public <E> List<E> getFiltered(Class<E> cl, Filter filter)
    {
        Criteria cr = createCriteria(cl, filter);
        return cr.list();
    }

    // ===========================================================================
    @Override
    public Criteria createCriteria(Class<?> cl, Filter filter)
    {
        Criteria cr = createCriteria(cl);

        for (FilterLine line : filter.getLines())
        {
            switch (line.getOperator())
            {
                case CONTAINS:
                    cr.add(Restrictions.like(line.getFieldName(), "%" + line.getValue() + "%"));
                    break;

                case DOES_NOT_CONTAIN:
                    cr.add(Restrictions.not(Restrictions.like(line.getFieldName(), "%" + line.getValue() + "%")));
                    break;

                case DOES_NOT_ICONTAIN:
                    cr.add(Restrictions.not(Restrictions.ilike(line.getFieldName(), "%" + line.getValue() + "%")));
                    break;

                case EQUALS:
                    cr.add(Restrictions.eq(line.getFieldName(), line.getValue()));
                    break;

                case GREATER_OR_EQUAL_THAN:
                    cr.add(Restrictions.ge(line.getFieldName(), line.getValue()));
                    break;

                case GREATER_THAN:
                    cr.add(Restrictions.gt(line.getFieldName(), line.getValue()));
                    break;

                case ICONTAINS:
                    cr.add(Restrictions.ilike(line.getFieldName(), "%" + line.getValue() + "%"));
                    break;

                case LESS_OR_EQUAL_THAN:
                    cr.add(Restrictions.le(line.getFieldName(), line.getValue()));
                    break;

                case LESS_THAN:
                    cr.add(Restrictions.lt(line.getFieldName(), line.getValue()));
                    break;

                case NOT_EQUALS:
                    cr.add(Restrictions.ne(line.getFieldName(), line.getValue()));
                    break;

                case ISNULL:
                    cr.add(Restrictions.isNull(line.getFieldName()));
                    break;

                case NOTNULL:
                    cr.add(Restrictions.isNotNull(line.getFieldName()));
                    break;

                default:
                    break;
            }
        }

        for (SortLine sort : filter.getSortLines())
        {
            if (sort.isAscending()) cr.addOrder(Property.forName(sort.getField()).asc());
            else cr.addOrder(Property.forName(sort.getField()).desc());
        }

        if (filter.getLimit() != 0) cr.setMaxResults(filter.getLimit());
        if (filter.getOffset() != 0) cr.setFirstResult(filter.getOffset());

        return cr;
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

    // ===========================================================================
    @Override
    public <E> long getCount(Class<E> cl)
    {
        Query q = createQuery("select count(*) from " + cl.getName());
        return (Long) q.uniqueResult();
    }

    // ===========================================================================
    @Override
    public Session getDelegate()
    {
        return delegate;
    }
}
