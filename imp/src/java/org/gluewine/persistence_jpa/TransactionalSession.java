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
package org.gluewine.persistence_jpa;

import java.io.Serializable;
import java.util.List;

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
     * Merges an existing entity, and returns the merged instance.
     *
     * @param o The entity to merge.
     * @return The merged instance.
     */
    Object merge(Object o);

    // ===========================================================================
    /**
     * Updates an existing entity.
     *
     * @param o The entity to update.
     */
    void update(Object o);

    // ===========================================================================
    /**
     * Returns the count for the given entity.
     *
     * @param <E> The generic Entity.
     * @param cl The class to process.
     * @return The number of entries.
     */
    <E> long getCount(Class<E> cl);

    // ===========================================================================
    /**
     * Returns the list of filtered entries.
     *
     * @param <E> The generic Entity.
     * @param cl The class to process.
     * @param filter The filter to apply.
     * @return The matching list.
     */
    <E> List<E> getFiltered(Class<E> cl, Filter filter);
}
