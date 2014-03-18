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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a method is annotated with @Rest it becomes available
 * through Rest calls.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface REST
{
    // ===========================================================================
    /**
     * Defines the path that needs to be used to access the annotated method.
     *
     * @return The path.
     */
    String path();

    // ===========================================================================
    /**
     * Defines whether the POST is done using a form. (needed for file transfer).
     *
     * @return True if a form is required.
     */
    boolean form() default false;
}
