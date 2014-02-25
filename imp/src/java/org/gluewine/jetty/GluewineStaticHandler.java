/**************************************************************************
 *
 * Gluewine Jetty Module
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
package org.gluewine.jetty;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * This handler allows to serve static files stored in directories.
 * Each directory specified in the properties file runs in its own context.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineStaticHandler extends ResourceHandler
{
    // ===========================================================================
    /**
     * The context of this handler.
     */
    private String context = null;

    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance with the given context.
     *
     * @param context The context of this handler.
     */
    public GluewineStaticHandler(String context)
    {
        if (!context.startsWith("/")) context = "/" + context;
        this.context = context;
    }

    // ===========================================================================
    /**
     * This method is overriden so that the context is removed from the path.
     *
     * @param path The path to retrieve.
     * @return The Resource or null if the resource could not be found.
     * @throws MalformedURLException If an error occurs.
     */
    @Override
    public Resource getResource(String path) throws MalformedURLException
    {
        try
        {
            if (path.startsWith(context)) path = path.substring(context.length());
            if (path.equals("")) path = "/";
            logger.debug("Request for resource: " + path);
            return super.getResource(path);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            throw e;
        }
    }
}
