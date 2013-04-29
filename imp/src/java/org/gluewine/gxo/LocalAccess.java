package org.gluewine.gxo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Static class that allows to register the streams for local usage.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class LocalAccess
{
    // ===========================================================================
    /**
     * The client input stream.
     */
    private PipedInputStream clientInput = new PipedInputStream();

    /**
     * The client output stream.
     */
    private PipedOutputStream clientOutput = new PipedOutputStream();

    /**
     * The server input stream.
     */
    private PipedInputStream serverInput = new PipedInputStream();

    /**
     * The server output stream.
     */
    private PipedOutputStream serverOutput = new PipedOutputStream();

    /**
     * The singleton instance to use.
     */
    private static LocalAccess instance = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @throws IOException If an error occurs connecting the streams.
     */
    private LocalAccess() throws IOException
    {
        clientInput.connect(serverOutput);
        serverInput.connect(clientOutput);
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the client.
     *
     * @return The input stream.
     */
    public InputStream getClientInputStream()
    {
        return clientInput;
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the server.
     *
     * @return The input stream.
     */
    public InputStream getServerInputStream()
    {
        return serverInput;
    }

    // ===========================================================================
    /**
     * Returns the outputstream to write to the client.
     *
     * @return The output stream.
     */
    public OutputStream getClientOutputStream()
    {
        return clientOutput;
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the server.
     *
     * @return The input stream.
     */
    public OutputStream getServerOutputStream()
    {
        return serverOutput;
    }

    // ===========================================================================
    /**
     * Returns the instance to use.
     *
     * @return The instance.
     * @throws IOException If an error occurs connecting the streams.
     */
    public static synchronized LocalAccess getInstance() throws IOException
    {
        if (instance == null) instance = new LocalAccess();
        return instance;
    }

    // ===========================================================================
    /**
     * Closes the streams.
     *
     * @throws IOException Thrown if an error occurs.
     */
    public void close() throws IOException
    {
        clientInput.close();
        clientOutput.close();
        serverInput.close();
        serverOutput.close();
    }
}
