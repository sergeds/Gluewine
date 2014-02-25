/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
