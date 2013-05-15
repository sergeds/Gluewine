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
    private PipedInputStream clientInput = null;

    /**
     * The client output stream.
     */
    private PipedOutputStream clientOutput = null;

    /**
     * The server input stream.
     */
    private PipedInputStream serverInput = null;

    /**
     * The server output stream.
     */
    private PipedOutputStream serverOutput = null;

    /**
     * The singleton instance to use.
     */
    private static LocalAccess instance = null;

    /**
     * Flag indicating whether the streams are connected.
     */
    private boolean connected = false;

    /**
     * Flag indicating whether the streams are closed.
     */
    private boolean closed = false;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    private LocalAccess()
    {
    }

    // ===========================================================================
    /**
     * Connects the streams.
     *
     * @throws IOException If an error occurs connecting the streams.
     */
    private synchronized void connect() throws IOException
    {
        if (!connected)
        {
            clientInput = new PipedInputStream();
            clientOutput = new PipedOutputStream();
            serverInput = new PipedInputStream();
            serverOutput = new PipedOutputStream();

            clientInput.connect(serverOutput);
            serverInput.connect(clientOutput);
            connected = true;
            closed = false;
        }
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the client.
     *
     * @return The input stream.
     * @throws IOException If an error occurs connecting the streams.
     */
    public InputStream getClientInputStream() throws IOException
    {
        connect();
        return clientInput;
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the server.
     *
     * @return The input stream.
     * @throws IOException If an error occurs connecting the streams.
     */
    public InputStream getServerInputStream() throws IOException
    {
        connect();
        return serverInput;
    }

    // ===========================================================================
    /**
     * Returns the outputstream to write to the client.
     *
     * @return The output stream.
     * @throws IOException If an error occurs connecting the streams.
     */
    public OutputStream getClientOutputStream() throws IOException
    {
        connect();
        return clientOutput;
    }

    // ===========================================================================
    /**
     * Returns the inputstream to read from the server.
     *
     * @return The input stream.
     * @throws IOException If an error occurs connecting the streams.
     */
    public OutputStream getServerOutputStream() throws IOException
    {
        connect();
        return serverOutput;
    }

    // ===========================================================================
    /**
     * Returns the instance to use.
     *
     * @return The instance.
     */
    public static synchronized LocalAccess getInstance()
    {
        if (instance == null) instance = new LocalAccess();
        return instance;
    }

    // ===========================================================================
    /**
     * Returns true if the closed method has been invoked.
     *
     * @return True if is has been closed.
     */
    public boolean isClosed()
    {
        return closed;
    }

    // ===========================================================================
    /**
     * Closes the streams.
     *
     * @throws IOException Thrown if an error occurs.
     */
    public synchronized void close() throws IOException
    {
        if (connected)
        {
            connected = false;
            closed = true;
            clientInput.close();
            clientOutput.close();
            serverInput.close();
            serverOutput.close();
        }
    }
}
