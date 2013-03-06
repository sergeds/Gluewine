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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * InputStream that uses the given CodeSource to resolve classes.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineObjectInputStream extends ObjectInputStream
{
    // ===========================================================================
    /**
     * The code source to use.
     */
    private CodeSource codeSource = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param in The inputsteam.
     * @param cs The code source to use.
     * @throws IOException If an error occurs opening the stream.
     */
    public GluewineObjectInputStream(InputStream in, CodeSource cs) throws IOException
    {
        super(in);
        this.codeSource = cs;
    }

    // ===========================================================================
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
    {
        Class<?> result = null;
        try
        {
            result = super.resolveClass(desc);
        }
        catch (Throwable e)
        {
            result = codeSource.getSourceClassLoader().loadClass(desc.getName());
        }

        if (result == null)
            throw new ClassNotFoundException("The class " + desc.getName() + " could not be found.");

        else
            return result;
    }
}
