/**************************************************************************
 *
 * Gluewine REST Module
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
package org.gluewine.rest;

import java.lang.reflect.Method;

/**
 * Simple bean that keeps track of a REST annotated method,
 * and the object it belongs to.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class RESTMethod
{
    // ===========================================================================
    /**
     * The annotated method.
     */
    private Method method = null;

    /**
     * The owner of the method.
     */
    private Object object = null;

    /**
     * Flag indicating that the parameters are in a form.
     */
    private boolean form = false;

    // ===========================================================================
    /**
     * @return the method.
     */
    public Method getMethod()
    {
        return method;
    }

    // ===========================================================================
    /**
     * @param method the method to set.
     */
    public void setMethod(Method method)
    {
        this.method = method;
    }

    // ===========================================================================
    /**
     * @return the object.
     */
    public Object getObject()
    {
        return object;
    }

    // ===========================================================================
    /**
     * @param object the object to set.
     */
    public void setObject(Object object)
    {
        this.object = object;
    }

    // ===========================================================================
    /**
     * @return the form.
     */
    public boolean isForm()
    {
        return form;
    }

    // ===========================================================================
    /**
     * @param form the form to set.
     */
    public void setForm(boolean form)
    {
        this.form = form;
    }
}
