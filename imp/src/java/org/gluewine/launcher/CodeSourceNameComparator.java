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
