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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * Compares two directories based on their name (lowercased).
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CodeSourceNameComparator implements Comparator<CodeSource>, Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -1549551523009469888L;

    // ===========================================================================
    @Override
    public int compare(CodeSource o1, CodeSource o2)
    {
        String n1 = o1.getDisplayName().toLowerCase(Locale.getDefault());
        String n2 = o2.getDisplayName().toLowerCase(Locale.getDefault());
        return n1.compareTo(n2);
    }
}
