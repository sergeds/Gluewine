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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Stream wrapper that decompresses the data read before returning it.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CompressedBlockInputStream extends FilterInputStream
{
    // ===========================================================================
    /**
     * Buffer of compressed data read from the stream.
     */
    private byte[] inBuf = null;

    /**
     * Length of data in the input data.
     */
    private int inLength = 0;

    /**
     * Buffer of uncompressed data.
     */
    private byte[] outBuf = null;

    /**
     * Offset of uncompressed data.
     */
    private int outOffs = 0;

    /**
     * Length of uncompressed data.
     */
    private int outLength = 0;

    /**
     * Inflater for decompressing.
     */
    private Inflater inflater = null;

    /**
     * The number of bytes read.
     */
    private long bytesRead = 0;

    /**
     * The number of bytes compressed.
     */
    private long bytesCompressed = 0;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param is The stream to wrap.
     *
     * @throws IOException If an error occurs.
     */
    public CompressedBlockInputStream(InputStream is) throws IOException
    {
        super(is);
        inflater = new Inflater();
    }

    // ===========================================================================
    /**
     * Reads an decompresses the data.
     *
     * @throws IOException If an error occurs reading from the wrapped stream.
     */
    private void readAndDecompress() throws IOException
    {
        // Read the length of the compressed block
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        inLength = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

        bytesCompressed += inLength;

        ch1 = in.read();
        ch2 = in.read();
        ch3 = in.read();
        ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        outLength = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

        bytesRead += outLength;

        // Make sure we've got enough space to read the block
        if ((inBuf == null) || (inLength > inBuf.length))
        {
            inBuf = new byte[inLength];
        }

        if ((outBuf == null) || (outLength > outBuf.length))
        {
            outBuf = new byte[outLength];
        }

        // Read until we're got the entire compressed buffer.
        // read(...) will not necessarily block until all
        // requested data has been read, so we loop until
        // we're done.
        int inOffs = 0;
        while (inOffs < inLength)
        {
            int inCount = in.read(inBuf, inOffs, inLength - inOffs);
            if (inCount == -1)
            {
                throw new EOFException();
            }
            inOffs += inCount;
        }

        inflater.setInput(inBuf, 0, inLength);
        try
        {
            inflater.inflate(outBuf);
        }
        catch (DataFormatException dfe)
        {
            throw new IOException("Data format exception - " + dfe.getMessage());
        }

        // Reset the inflator so we can re-use it for the
        // next block
        inflater.reset();

        outOffs = 0;
    }

    // ===========================================================================
    /**
     * Returns the number of bytes read. (uncompressed).
     *
     * @return The number of bytes.
     */
    public long getBytesRead()
    {
        return bytesRead;
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
    public int read() throws IOException
    {
        if (outOffs >= outLength)
        {
            try
            {
                readAndDecompress();
            }
            catch (EOFException eof)
            {
                return -1;
            }
        }

        return outBuf[outOffs++] & 0xff;
    }

    // ===========================================================================
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int count = 0;
        while (count < len)
        {
            if (outOffs >= outLength)
            {
                try
                {
                    // If we've read at least one decompressed
                    // byte and further decompression would
                    // require blocking, return the count.
                    if ((count > 0) && (in.available() == 0))
                        return count;
                    else
                        readAndDecompress();
                }
                catch (EOFException eof)
                {
                    if (count == 0)
                        count = -1;
                    return count;
                }
            }

            int toCopy = Math.min(outLength - outOffs, len - count);
            System.arraycopy(outBuf, outOffs, b, off + count, toCopy);
            outOffs += toCopy;
            count += toCopy;
        }

        return count;
    }

    // ===========================================================================
    @Override
    public int available() throws IOException
    {
        // This isn't precise, but should be an adequate
        // lower bound on the actual amount of available data
        return (outLength - outOffs) + in.available();
    }
}