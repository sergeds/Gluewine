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

import org.hibernate.Criteria;


/**
 * This interface defines a PreProcessor that is invoked on a Criteria
 * and the class the criteria has been initialized on.
 *
 * The preprocessor is allowed to perform all kinds of transformations or
 * additions on the given Criteria, and must return it (or another instance).
 *
 * All registered QueryPreProcessors are processed and there is no guarantee
 * on the order the registered PreProcessors are invoked. So no order
 * assumptions should be made !
 *
 * The QueryPreProcessors (and the QueryPostProcessors) can be used to
 * dynamiccaly create Views, SecurityChecks, Debugging info, ...
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface QueryPreProcessor
{
    // ===========================================================================
    /**
     * Requests the processor to PreProcess the given criteria.
     * The class given is the class used to create the initial Criteria.
     * The is no guarantee that the Criteria given is still associated
     * with the given Class, as a PreProcessor could have returned a
     * totally different Criteria.
     *
     * @param criteria The criteria to preprocess.
     * @param clazz The clazz used to create the initial Criteria.
     * @return The preprocessed Criteria.
     */
    Criteria preProcess(Criteria criteria, Class<?> clazz);
}
