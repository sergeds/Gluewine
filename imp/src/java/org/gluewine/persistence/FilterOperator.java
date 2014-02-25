/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence;

/**
 * Defines all possible operators for filters.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public enum FilterOperator
{
    // ===========================================================================
    /**
     * The strictly less than operator.
     */
    LESS_THAN,

    /**
     * The less or equal than operator.
     */
    LESS_OR_EQUAL_THAN,

    /**
     * The strictly greater than operator.
     */
    GREATER_THAN,

    /**
     * The greater or equal than operator.
     */
    GREATER_OR_EQUAL_THAN,

    /**
     * The equals operator.
     */
    EQUALS,

    /**
     * The not equals operator.
     */
    NOT_EQUALS,

    /**
     * The contains (case sensitive) operator.
     */
    CONTAINS,

    /**
     * The contains (case insensitive) operator.
     */
    ICONTAINS,

    /**
     * The does not contain (case sensitive) operator.
     */
    DOES_NOT_CONTAIN,

    /**
     * The does not contain (case insensitive) operator.
     */
    DOES_NOT_ICONTAIN,

    /**
     * The field is null.
     */
    ISNULL,

    /**
     * The field is not null.
     */
    NOTNULL;
}
