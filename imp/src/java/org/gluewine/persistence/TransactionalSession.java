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
     * @throws PersistenceException If a problem occurs.
     */
    Serializable add(Object o) throws PersistenceException;

    // ===========================================================================
    /**
     * Adds or updates an entity depending on whether it already is present in the
     * database or not.
     *
     * @param o The object to add or update.
     * @throws PersistenceException If a problem occurs.
     */
    void addOrUpdate(Object o) throws PersistenceException;

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
     * Deletes the given object.
     *
     * @param o The object to delete.
     * @throws PersistenceException If a problem occurs.
     */
    void delete(Object o) throws PersistenceException;

    // ===========================================================================
    /**
     * Deletes an object of the given type using the id specified.
     *
     * @param type The type of object.
     * @param id The id of the object.
     * @throws PersistenceException If a problem occurs.
     */
    void delete(String type, Object id) throws PersistenceException;

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
     * @throws PersistenceException If a problem occurs.
     */
    Object get(Class<?> cl, Serializable id) throws PersistenceException;

    // ===========================================================================
    /**
     * Returns all entities of the given class.
     *
     * @param <E> The generic class to retrieve.
     * @param cl The class to process.
     * @return The list of entities.
     * @throws PersistenceException If a problem occurs.
     */
    <E> List<E> getAll(Class<E> cl) throws PersistenceException;

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
     * @throws PersistenceException If a problem occurs.
     */
    <E> List<E> getAllSorted(Class<E> cl, String sortField, boolean ascending) throws PersistenceException;

    // ===========================================================================
    /**
     * Updates an existing entity.
     *
     * @param o The entity to update.
     * @throws PersistenceException If a problem occurs.
     */
    void update(Object o) throws PersistenceException;
}
