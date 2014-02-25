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
