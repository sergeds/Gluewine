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

import java.net.URL;

/**
 * The Gluewine classloader is a classloader that can dispatch load requests
 * to other embedded classloaders if it cannot fullfill the request.
 *
 * Gluewine uses 1 classloader instance per JAR file so that those jar files
 * can easily be updated, removed or added.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface GluewineClassLoader
{
    // ===========================================================================
    /**
     * Requests to load the class with the given name. If the loader cannot fullfill
     * the request, it should dispatch the call if the dispatch flag is set to true.
     *
     * <br>Note that the call should never be dispatched to the invoker.
     *
     * @param name The name of the class to load.
     * @param dispatch If true the call can be dispatched.
     * @param invoker The classloader that dispatched this call.
     * @return The class.
     * @throws ClassNotFoundException Thrown if the class could not be found.
     */
    Class<?> loadClass(String name, boolean dispatch, ClassLoader invoker) throws ClassNotFoundException;

    // ===========================================================================
    /**
     * Loads the resource with the given name and return a URL to it. If the
     * resource cannot be found, and the dispatch flag is set to true, the
     * call can be dispatched.
     *
     * <br>Note that the call should never be dispatched to the invoker.
     *
     * @param resource The resource to load.
     * @param invoker The classloader that dispatched this call.
     * @return The (possibly null) URL.
     */
    URL loadResource(String resource, boolean dispatch, ClassLoader invoker);

    // ===========================================================================
    /**
     * Returns true if the classloader can handle class from the given package name.
     *
     * @param packName The name to check.
     * @return True if the package can be handled.
     */
    boolean canProvidePackage(String packName);

    // ===========================================================================
    /**
     * Returns true if the classloader can handle resources with the given path.
     *
     * @param path The path to check.
     * @return True if the path can be handled.
     */
    boolean canProvidePath(String path);
}
