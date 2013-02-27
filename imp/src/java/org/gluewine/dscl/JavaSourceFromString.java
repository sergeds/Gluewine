/**************************************************************************
 *
 * Gluewine DSCL Enhancer Module
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
package org.gluewine.dscl;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Allows to compile java code stored in a String.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JavaSourceFromString extends SimpleJavaFileObject
{
    // ===========================================================================
    /**
     * The actual code.
     */
    private String code = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param className The name of the class.
     * @param code The actual code.
     */
    public JavaSourceFromString(String className, String code)
    {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    // ===========================================================================
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return code;
    }
}
