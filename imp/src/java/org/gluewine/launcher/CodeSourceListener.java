/**************************************************************************
 *
 * Gluewine Launcher Module
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
