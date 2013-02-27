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

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * Compares files.
 *
 * If the parent directory of a file is named fix, it will be in front.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JarComparator implements Comparator<File>, Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -4336225130752369727L;

    // ===========================================================================
    @Override
    public int compare(File o1, File o2)
    {
        String n1 = o1.getName().toLowerCase(Locale.getDefault());
        String n2 = o2.getName().toLowerCase(Locale.getDefault());
        String p1 = o1.getParentFile().getName().toLowerCase(Locale.getDefault());
        String p2 = o2.getParentFile().getName().toLowerCase(Locale.getDefault());

        if (p1.equals("fix") && !p2.equals("fix")) return -1;
        else if (!p1.equals("fix") && p2.equals("fix")) return 1;
        else return n1.compareTo(n2);
    }

}
