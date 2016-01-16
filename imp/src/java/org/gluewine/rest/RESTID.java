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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used for the parameters of an REST annotated method,
 * allowing to specify an ID to those parameters, that then must match
 * the name of GET parameters or a POST object.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RESTID
{
    // ===========================================================================
    /**
     * Returns the id of the parameter.
     */
    String id();

    /**
     * If true, the parameter value will be retrieved from a HTTP header instead of
     * a POST or GET field.
     */
    boolean header() default false;

    /**
     * If true, the parameter value will be the entire HTTP body. Don't combine this with regular non-header and non-body RESTID.
     */
    boolean body() default false;

    /**
     * If true, the parameter value will be the HTTP request method (GET, PUT, ...).
     */
    boolean method() default false;

    /**
     * If true, the parameter value will be the provided mime type of the file field. This only makes sense for file upload fields in forms.
     */
    boolean mimetype() default false;

    /**
     * If true, the parameter value will be the provided filename of the file field. This only makes sense for file upload fields in forms.
     */
    boolean filename() default false;
}
