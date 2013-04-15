/**************************************************************************
 *
 * Gluewine Console Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.console;

/**
 * Defines ANSI codes that can be used to output colored or attributed text.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface AnsiCodes
{
    /**
     * The escape code.
     */
    String E = "\u001b[";

    /**
     * Black color.
     */
    String BLACK = E + "30m";

    /**
     * Red color.
     */
    String RED = E + "31m";

    /**
     * Clears the screen.
     */
    String CLS = E + "2J";

    /**
     * Cursor on 1,1 of screen.
     */
    String HOME = E + "1;1H";
}
