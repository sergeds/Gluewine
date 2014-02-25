/**************************************************************************
 *
 * Gluewine GWT Integration Module
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
