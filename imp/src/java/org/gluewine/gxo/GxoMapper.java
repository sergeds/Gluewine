/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This mapper will deserialize OSGi named classes to the
 * correspondant GWT class name.
 *
 * This means that the package name of the requested class is removed
 * and replaced by the root package passed on in the constructor.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GxoMapper extends MapperWrapper
{
    // ===========================================================================
    /**
     * The root of the 'replacement' package.
     */
    private String rootPackage = null;

    /**
     * List of package names that should be left unchanged.
     */
    private List<String> unchangedPackages = new ArrayList<String>();

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param wrapped The wrapped mapper.
     * @param rootPackage The root package to use.
     */
    public GxoMapper(Mapper wrapped, String rootPackage)
    {
        super(wrapped);
        this.rootPackage = rootPackage;

        unchangedPackages.add("java.");
        unchangedPackages.add("javax.");
        unchangedPackages.add("org.gluewine.");
    }

    // ===========================================================================
    @Override
    public Class<?> realClass(String elementName)
    {
        boolean change = true;
        for (int i = 0; i < unchangedPackages.size() && change; i++)
        {
            if (elementName.startsWith(unchangedPackages.get(i)))
                change = false;
        }

        String n = elementName;
        if (change)
        {
            int i = n.lastIndexOf('.');

            if (i > 0)
                n = rootPackage + n.substring(i + 1);
        }

        return super.realClass(n);
    }
}
