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
