/**************************************************************************
 *
 * Gluewine DSCL Enhancer Module
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
package org.gluewine.dscl;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import org.apache.log4j.Logger;

/**
 * The DiagnosticListener to use when submitting compilation jobs.
 * It reports all diagnostics to the Console logger.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DSCLDiagnosticListener implements DiagnosticListener<JavaFileObject>
{
    // ===========================================================================
    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic)
    {
        StringBuilder b = new StringBuilder();

        b.append(diagnostic.getKind()).append(": ");
        b.append(diagnostic.getMessage(Locale.getDefault()));
        b.append(" at position: ").append(diagnostic.getLineNumber()).append("/").append(diagnostic.getColumnNumber());

        try
        {
            CharSequence seq = diagnostic.getSource().getCharContent(true);
            b.append('\n');
            b.append(seq.subSequence((int) diagnostic.getStartPosition(), (int) diagnostic.getEndPosition())).append('\n');
            for (long i = diagnostic.getStartPosition(); i  < diagnostic.getPosition(); i++)
                b.append(' ');
            b.append('^');
            for (long i = diagnostic.getPosition(); i  < diagnostic.getEndPosition(); i++)
                b.append(' ');
        }
        catch (Throwable e)
        {
            logger.error(e);
        }

        logger.error("DSCLCompiler: " + b.toString());
    }
}
