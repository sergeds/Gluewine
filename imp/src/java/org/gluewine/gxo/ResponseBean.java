/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;


/**
 * The response to an ExecBean.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ResponseBean extends GxoBean
{
    // ===========================================================================
    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The actual response.
     */
    private Object response = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param id The id.
     */
    public ResponseBean(String id)
    {
        super(id);
    }

    // ===========================================================================
    /**
     * Returns the response.
     *
     * @return The response.
     */
    public Object getResponse()
    {
        return response;
    }

    // ===========================================================================
    /**
     * Sets the response.
     *
     * @param response The response.
     */
    public void setResponse(Object response)
    {
        this.response = response;
    }
}
