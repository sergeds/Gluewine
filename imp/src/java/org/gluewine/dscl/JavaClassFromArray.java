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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Can be used to store the results of a compilation into an array.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JavaClassFromArray extends SimpleJavaFileObject
{
    // ===========================================================================
    /**
     * The array to write to.
     */
    private ByteArrayOutputStream output = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param className The name of the class.
     */
    public JavaClassFromArray(String className)
    {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    // ===========================================================================
    @Override
    public OutputStream openOutputStream() throws IOException
    {
        if (output == null)
            output = new ByteArrayOutputStream();

        return output;
    }

    // ===========================================================================
    /**
     * Returns the content that was written.
     *
     * @return The content.
     */
    public byte[] getClassContent()
    {
        return output.toByteArray();
    }
}
