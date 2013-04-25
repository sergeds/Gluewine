package org.gluewine.jetty;

import java.net.MalformedURLException;

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
        if (path.startsWith(context)) path = path.substring(context.length());
        return super.getResource(path);
    }
}
