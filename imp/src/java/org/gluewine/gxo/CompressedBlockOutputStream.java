/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * Outputstream wrapper that compresses the data before sending it out.
 * The stream uses a buffer, and will only send the compressed data when
 * the buffer has been filled, the stream flushed or closed.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CompressedBlockOutputStream extends FilterOutputStream
{
    // ===========================================================================
    /**
     * Buffer for input data.
     */
    private byte[] inBuf = null;

    /**
     * Buffer for compressed data to be written.
     */
    private byte[] outBuf = null;

    /**
     * Number of bytes in the buffer.
     */
    private int len = 0;

    /**
     * Deflater for compressing data.
     */
    private Deflater deflater = null;

    /**
     * The number of bytes written.
     */
    private long bytesWritten = 0;

    /**
     * The number of compressed bytes.
     */
    private long bytesCompressed = 0;

    // ===========================================================================
    /**
     * Constructs a CompressedBlockOutputStream that writes to the given underlying output stream 'os' and
     * sends a compressed block once 'size' byte have been written.
     * The default compression strategy and level are used.
     *
     * @param os The Outputstream to wrap.
     * @param size The max size of the buffer.
     * @throws IOException If an error occurs sending the data on the network.
     */
    public CompressedBlockOutputStream(OutputStream os, int size) throws IOException
    {
        this(os, size, Deflater.DEFAULT_COMPRESSION, Deflater.DEFAULT_STRATEGY);
    }

    // ===========================================================================
    /**
     * Constructs a CompressedBlockOutputStream that writes to the given underlying output stream 'os' and
     * sends a compressed block once 'size' byte have been written.
     * The compression level and strategy should be specified using the constants defined in java.util.zip.Deflater.
     *
     * @param os The OutputStream to wrap.
     * @param size The buffer size.
     * @param level The compression level to use.
     * @param strategy The compression Strategy.
     * @throws IOException If an error occurs sending the data on the network.
     */
    public CompressedBlockOutputStream(OutputStream os, int size, int level, int strategy) throws IOException
    {
        super(os);
        this.inBuf = new byte[size];
        this.outBuf = new byte[size + 64];
        this.deflater = new Deflater(level);
        this.deflater.setStrategy(strategy);
    }

    // ===========================================================================
    /**
     * Compresses the data and sends it over the wrapped stream.
     *
     * @throws IOException If a problem occurs.
     */
    protected void compressAndSend() throws IOException
    {
        if (len > 0)
        {
            deflater.setInput(inBuf, 0, len);
            deflater.finish();
            int size = deflater.deflate(outBuf);

            // Write the size of the compressed data, followed
            // by the size of the uncompressed data
            out.write((size >> 24) & 0xFF);
            out.write((size >> 16) & 0xFF);
            out.write((size >> 8) & 0xFF);
            out.write((size >> 0) & 0xFF);

            out.write((len >> 24) & 0xFF);
            out.write((len >> 16) & 0xFF);
            out.write((len >> 8) & 0xFF);
            out.write((len >> 0) & 0xFF);

            bytesCompressed += size;
            bytesWritten += len;

            out.write(outBuf, 0, size);
            out.flush();

            len = 0;
            deflater.reset();
        }
    }

    // ===========================================================================
    /**
     * Returns the number of bytes written. (uncompressed).
     *
     * @return The number of bytes.
     */
    public long getBytesWritten()
    {
        return bytesWritten;
    }

    // ===========================================================================
    /**
     * Returns the number of bytes compressed.
     *
     * @return The number of bytes.
     */
    public long getBytesCompressed()
    {
        return bytesCompressed;
    }

    // ===========================================================================
    @Override
    public void write(int b) throws IOException
    {
        inBuf[len++] = (byte) b;
        if (len == inBuf.length)
        {
            compressAndSend();
        }
    }

    // ===========================================================================
    @Override
    public void write(byte[] b, int boff, int blen) throws IOException
    {
        while ((len + blen) > inBuf.length)
        {
            int toCopy = inBuf.length - len;
            System.arraycopy(b, boff, inBuf, len, toCopy);
            len += toCopy;
            compressAndSend();
            boff += toCopy;
            blen -= toCopy;
        }
        System.arraycopy(b, boff, inBuf, len, blen);
        len += blen;
    }

    // ===========================================================================
    @Override
    public void flush() throws IOException
    {
        compressAndSend();
        out.flush();
    }

    // ===========================================================================
    @Override
    public void close() throws IOException
    {
        compressAndSend();
        out.close();
    }
}