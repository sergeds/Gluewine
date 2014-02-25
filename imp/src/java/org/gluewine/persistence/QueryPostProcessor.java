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
