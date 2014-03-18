/**************************************************************************
 *
 * Gluewine Trace Module
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
package org.gluewine.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.gluewine.utils.ErrorLogger;

/**
 * Allows to trace services, and outputs the trace information in XML Files.
 * (one file per thread, allowing to preserve correct method invocation tree).
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class XMLTracer extends AbstractTracer
{
    // ===========================================================================
    /**
     * The map of XML Streamwriters to use. (indexed on the thread).
     */
    private Map<Thread, XMLStreamWriter> writers = new HashMap<Thread, XMLStreamWriter>();

    /**
     * The file to write to.
     */
    private String file = null;

    /**
     * The factory to use.
     */
    private XMLOutputFactory xof =  XMLOutputFactory.newInstance();

    /**
     * The date formatter to use.
     */
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param file The file to write to.
     */
    XMLTracer(String file)
    {
        String lf = file.toLowerCase(Locale.getDefault());
        int i = lf.indexOf(".xml");
        if (i > 0) file = file.substring(0, i);
        this.file = file;
    }

    // ===========================================================================
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
        if (isSuppressed()) return;

        XMLStreamWriter writer = getWriter();
        if (writer != null)
        {
            try
            {
                writer.writeStartElement("method");
                writer.writeAttribute("class", getClassName(o.getClass()));
                writer.writeAttribute("name", m.getName());
                writer.writeAttribute("start", format.format(new Date()));

                for (Object p : params)
                {
                    if (p != null)
                    {
                        writer.writeStartElement("parameter");
                        writer.writeAttribute("class", getClassName(p.getClass()));
                        writer.writeCharacters(p.toString());
                        writer.writeEndElement();
                    }
                    else writer.writeEmptyElement("parameter");
                }

            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
            }
        }
        else System.out.println("No Writer");

        clearSuppression();;
    }

    // ===========================================================================
    /**
     * Returns the writer associated with the current thread. If none exists, one is
     * created.
     *
     * @return The writer to use.
     */
    private XMLStreamWriter getWriter()
    {
        XMLStreamWriter writer = writers.get(Thread.currentThread());

        if (writer == null)
        {
            try
            {
                writer = xof.createXMLStreamWriter(new FileWriterWithEncoding(file + "_" + Thread.currentThread().getId() + ".xml", "utf8"));
                writer.writeStartDocument("utf-8","1.0");
                writer.writeStartElement("trace");
                writer.writeAttribute("thread", Long.toString(Thread.currentThread().getId()));
                writers.put(Thread.currentThread(), writer);
            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
            }
        }

        return writer;
    }

    // ===========================================================================
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        if (isSuppressed()) return;

        XMLStreamWriter writer = getWriter();
        if (writer != null)
        {
            try
            {
                if (!m.getReturnType().equals(Void.TYPE))
                {
                    writer.writeStartElement("result");
                    if (result != null) writer.writeCharacters(result.toString());
                    else writer.writeCharacters("null");
                    writer.writeEndElement();
                }
            }
            catch (Throwable e)
            {
                ErrorLogger.log(getClass(), e);
            }
        }

        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
        if (isSuppressed()) return;

        XMLStreamWriter writer = getWriter();
        if (writer != null)
        {
            try
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                writer.writeStartElement("exception");
                writer.writeCharacters(sw.toString());
                writer.writeEndElement();
            }
            catch (Throwable t)
            {
                ErrorLogger.log(getClass(), t);
            }
        }

        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void after(Object o, Method m, Object[] params)
    {
        if (isSuppressed()) return;

        XMLStreamWriter writer = getWriter();
        if (writer != null)
        {
            try
            {
                writer.writeEndElement();
                writer.flush();
            }
            catch (Throwable t)
            {
                ErrorLogger.log(getClass(), t);
            }
        }

        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void close()
    {
        isSuppressed();
        for (XMLStreamWriter writer : writers.values())
        {
            try
            {
                writer.writeEndElement(); // Close the root.
                writer.writeEndDocument();
                writer.flush();
                writer.close();
            }
            catch (XMLStreamException e)
            {
                ErrorLogger.log(getClass(), e);
            }
        }

        writers.clear();
    }
}
