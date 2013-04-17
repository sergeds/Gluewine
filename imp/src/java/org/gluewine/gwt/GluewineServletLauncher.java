package org.gluewine.gwt;


/**
 * The class is registered in the manifest of the bundle as a GluewinService,
 * and its only purpose is to set a System variable so that the AbstractGluewineService
 * knows that it runs locally.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineServletLauncher
{
    // ===========================================================================
    /**
     * Creates an instance.
     */
    public GluewineServletLauncher()
    {
        System.setProperty("gluewine.gxolocal", "true");
    }
}
