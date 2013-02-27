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

/**
 * <p>Defines processors that are invoked after a (write) Query has been executed.
 * (but before the transaction is committed).
 *
 * <p>All registered processors are invoked and there is no guarantee made on
 * the order of invocation.
 * So, no assumptions should be made regarding the order.
 *
 * <p>As mentioned this is only invoked after a write query has been executed:
 * <br>- delete()
 * <br>- persist()
 * <br>- save()
 * <br>- update()
 * <br>- saveOrUpdate()
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface QueryPostProcessor
{
    // ===========================================================================
    /**
     * Invoked when the object given has been deleted. (Ie. when the delete()
     * method of the session has been invoked).
     *
     * @param id The object's identifier.
     * @param o The object that has been deleted.
     */
    void deleted(Serializable id, Object o);

    // ===========================================================================
    /**
     * Invoked when the object givan has been persisted. (Ie. when the persist()
     * method of the session has been invoked).
     *
     * @param id The object's identifier.
     * @param o The object that has been persisted.
     */
    void persisted(Serializable id, Object o);

    // ===========================================================================
    /**
     * Invoked when the object given has been added. (Ie. when the save() method of
     * the session has been invoked).
     *
     * @param id The object's identifier.
     * @param o The object that has been saved.
     */
    void added(Serializable id, Object o);

    // ===========================================================================
    /**
     * Invoked when the object given has been added or updated. (Ie. when the
     * saveOrUpdate() method of the session has been invoked).
     *
     * @param id The object's identifier.
     * @param o The object that has been saved or updated.
     */
    void addedOrUpdated(Serializable id, Object o);

    // ===========================================================================
    /**
     * Invoked when the object given has been updated. (Ie. when the update()
     * mehod of the session has been invoked).
     *
     * @param id The object's identifier.
     * @param o The object that has been update.
     */
    void updated(Serializable id, Object o);
}
