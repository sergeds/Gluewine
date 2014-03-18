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
