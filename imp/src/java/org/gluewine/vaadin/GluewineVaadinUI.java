/**************************************************************************
 *
 * Gluewine Vaadin Module
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
package org.gluewine.vaadin;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gluewine.core.Glue;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.jetty.GluewineJettyLauncher;
import org.gluewine.jetty.GluewineServletProperties;

import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Defines a Vaadin UI, that will initialize a VaadinServlet, register it with the
 * running Jetty instance.
 *
 * Classes extending this class should be initialized through a service definition.
 * (Gluewine-Services in the manifest of the extension).
 *
 * @author Gluewine/Serge de Schaetzen
 *
 */
public abstract class GluewineVaadinUI extends UI implements GluewineServletProperties
{
    // ===========================================================================
    /** The serial uid. */
    private static final long serialVersionUID = -2952093663571845661L;

    /** The jetty instance to register with. */
    @Glue
    private GluewineJettyLauncher launcher;

    /** The context path of the servlet.
     *  This is the path of the URL that allows to reach this UI.
     */
    private String context = null;

    /** The properties to assign to the VaadinServlet at startup. */
    @Glue(properties = "vaadin.properties")
    private Properties props;

    // ===========================================================================
    /**
     * Creats an instance.
     *
     * @param context The context to use when registering the servlet.
     */
    protected GluewineVaadinUI(String context)
    {
        this.context = context;
    }

    // ===========================================================================
    /**
     * Initializes the VaadinServlet and registers it with the Jetty instance.
     */
    @RunOnActivate
    public void intialize()
    {
        VaadinServlet servlet = new VaadinServlet();
        Map<String, String> params = new HashMap<String, String>();
        params.put(VaadinSession.UI_PARAMETER, getUnEnhancedClassName());
        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            params.put(key, props.getProperty(key));
        }
        launcher.register(context, servlet, params);
    }

    // ===========================================================================
    /**
     * Returns the name of the actual class. (not the enhanced one.)
     *
     * @return The class name.
     */
    private String getUnEnhancedClassName()
    {
        String name = null;
        Class<?> cl = getClass();
        while (cl != null && name == null)
        {
            if (cl.getName().indexOf("$$EnhancerByCGLIB$$") > -1) cl = cl.getSuperclass();
            else name = cl.getName();
        }

        return name;
    }

    // ===========================================================================
    /**
     * Deregisters the servlet, making this UI unreachable.
     */
    @RunOnDeactivate
    public void terminate()
    {
        launcher.unregister(context);
    }
}
