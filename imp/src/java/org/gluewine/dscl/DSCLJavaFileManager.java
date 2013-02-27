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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;


/**
 * JavaFileManager that uses JavaClassFromArray as output files.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DSCLJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    // ===========================================================================
    /**
     * The map of compiled classes.
     */
    private Map<String, JavaClassFromArray> classes = new HashMap<String, JavaClassFromArray>();

    /**
     * The wrapped filemanager.
     */
    private JavaFileManager fileManager = null;

    /**
     * The indexer to use.
     */
    private JarClassIndexer indexer = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param fileManager The fileManager to use.
     * @param indexer The indexer to use.
     */
    public DSCLJavaFileManager(JavaFileManager fileManager, JarClassIndexer indexer)
    {
        super(fileManager);
        this.fileManager = fileManager;
        this.indexer = indexer;
    }

    // ===========================================================================
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
    {
        JavaClassFromArray javaClass = new JavaClassFromArray(className);
        classes.put(className, javaClass);
        return javaClass;
    }

    // ===========================================================================
    /**
     * Returns the java class object for the given name.
     *
     * @param className The classname to process.
     * @return The (possibly null) java class object.
     */
    public JavaClassFromArray getJavaClass(String className)
    {
        return classes.get(className);
    }

    // ===========================================================================
    /**
     * Returns the map of java classes indexed on the classname.
     *
     * @return The map of classes.
     */
    public Map<String, JavaClassFromArray> getJavaClasses()
    {
        return classes;
    }

    // ===========================================================================
    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException
    {
        List<JavaFileObject> files = null;

        if (location.getName().equals("CLASS_PATH"))
            files = indexer.getListOfJavaFileObjects(packageName);

        if (files != null)
        {
            return files;
        }

        else
        {
            return fileManager.list(location, packageName, kinds, recurse);
        }
    }

    // ===========================================================================
    @Override
    public String inferBinaryName(Location location, JavaFileObject file)
    {
        if (file instanceof JarEntryJavaClassFile)
            return ((JarEntryJavaClassFile) file).getClassName();
        else
            return fileManager.inferBinaryName(location, file);
    }

    // ===========================================================================
    @Override
    public boolean hasLocation(Location location)
    {
        return location.isOutputLocation();
    }
}
