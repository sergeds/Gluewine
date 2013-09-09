package org.gluewine.gwt;


/**
 * The class is registered in the manifest of the bundle as a GluewinService,
 * and its only purpose is to set a System variable so that the AbstractGluewineService
 * knows that it runs locally.
 *
 * When the AbstractGluewineService instance is loaded in another environment than Gluewine
 * it will not be activated, not setting this property. The AbstractGluewineService will
 * hence know that the Gluewine environment is not local.
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
