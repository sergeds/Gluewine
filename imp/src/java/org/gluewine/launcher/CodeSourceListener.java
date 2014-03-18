/**************************************************************************
 *
 * Gluewine Launcher Module
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
package org.gluewine.launcher;

import java.util.List;


/**
 * Interface to be implemented by classes wanting to be notified
 * of the presence of new codesource or the removal of one.
 *
 * <p>Note that an update of existing files will result in the
 * invocation of <code>codeSourceRemove(codesources)</code> followed by the
 * invocation of <code>codeSourceAdded(codesources)</code>.
 *
 * <p>To register these listeners simply implement them (from a Glued service)
 * or register them with the repository.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface CodeSourceListener
{
    // ===========================================================================
    /**
     * Invoked when new codesources have been added to the network.
     *
     * @param sources The codesources that have been added.
     */
    void codeSourceAdded(List<CodeSource> sources);

    // ===========================================================================
    /**
     * Invoked when codesources have been removed from the network.
     *
     * @param sources The codesources that have been removed.
     */
    void codeSourceRemoved(List<CodeSource> sources);
}
