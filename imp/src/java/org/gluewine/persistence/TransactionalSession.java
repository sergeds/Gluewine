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
package org.gluewine.persistence;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.jdbc.Work;

/**
 * Defines a transactional session.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface TransactionalSession
{
    // ===========================================================================
    /**
     * Adds a new entity.
     *
     * @param o The new entity.
     * @return The entity id.
     */
    Serializable add(Object o);

    // ===========================================================================
    /**
     * Adds or updates an entity depending on whether it already is present in the
     * database or not.
     *
     * @param o The object to add or update.
     */
    void addOrUpdate(Object o);

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
     * Creates and returns a Criteria for the given entity, starting from the
     * given offset and limited to the specified amount.
     *
     * @param entity The entity to process.
     * @param offset The offset.
     * @param limit The max number of entries to return.
     * @return The Criteria.
     */
    Criteria createCriteria(Class<?> entity, int offset, int limit);

    // ===========================================================================
    /**
     * Deletes the given object.
     *
     * @param o The object to delete.
     */
    void delete(Object o);

    // ===========================================================================
    /**
     * Deletes an object of the given type using the id specified.
     *
     * @param type The type of object.
     * @param id The id of the object.
     */
    void delete(String type, Object id);

    // ===========================================================================
    /**
     * Executes the JDBC worker.
     *
     * @param work The worker to execute.
     */
    void doWork(Work work);

    // ===========================================================================
    /**
     * Returns the entity of the given class with the id specified.
     *
     * @param cl The class to process.
     * @param id The id.
     * @return The entity or null if no entity matches the id.
     */
    Object get(Class<?> cl, Serializable id);

    // ===========================================================================
    /**
     * Returns all entities of the given class.
     *
     * @param <E> The generic class to retrieve.
     * @param cl The class to process.
     * @return The list of entities.
     */
    <E> List<E> getAll(Class<E> cl);

    // ===========================================================================
    /**
     * Returns the entities of the given class starting from the given offset and
     * limited to the amount specified.
     *
     * @param <E> The generic class to retrieve.
     * @param cl The class to process.
     * @param offset The offset to start from.
     * @param limit The max number of entries to return.
     * @return The list of entities.
     */
    <E> List<E> getAll(Class<E> cl, int offset, int limit);

    // ===========================================================================
    /**
     * Returns all entities of the given class sorted according to the given
     * field.
     *
     * @param <E> The generic class to retrieve.
     * @param cl The class to process.
     * @param sortField The field to sort on.
     * @param ascending True to sort ascending, false to sort descending.
     * @return The sorted list of entities.
     */
    <E> List<E> getAllSorted(Class<E> cl, String sortField, boolean ascending);

    // ===========================================================================
    /**
     * Updates an existing entity.
     *
     * @param o The entity to update.
     */
    void update(Object o);

    // ===========================================================================
    /**
     * Creates a query object and returns it. Pre- and Post processors will not
     * be notified.
     *
     * @param query The query string.
     * @return
     */
    Query createQuery(String query);

    // ===========================================================================
    /**
     * Returns the count for the given entity.
     *
     * @param <E> The generic Entity.
     * @param cl The class to process.
     * @return The number of entries.
     */
    <E> long getCount(Class<E> cl);
}
