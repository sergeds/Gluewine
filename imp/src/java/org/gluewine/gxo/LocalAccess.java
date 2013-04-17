package org.gluewine.gxo;

import java.util.Stack;

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
     * The client stack.
     */
    private static Stack<String> client = new Stack<String>();

    /**
     * The server stack.
     */
    private static Stack<String> server = new Stack<String>();

    // ===========================================================================
    /**
     * Creates an instance.
     */
    private LocalAccess()
    {
    }

    // ===========================================================================
    /**
     * Writes an item to the server queue.
     *
     * @param item The item to write.
     */
    public static void writeToServer(String item)
    {
        synchronized (server)
        {
            server.push(item);
            server.notifyAll();
        }
    }

    // ===========================================================================
    /**
     * Writes an item to the client queue.
     *
     * @param item The item to write.
     */
    public static void writeToClient(String item)
    {
        synchronized (client)
        {
            client.push(item);
            client.notifyAll();
        }
    }

    // ===========================================================================
    /**
     * Reads and returns a String. This method blocks until a String is available.
     *
     * @return The string to read.
     * @throws InterruptedException If a problem occurs.
     */
    public static String readFromServer() throws InterruptedException
    {
        synchronized (server)
        {
            while (server.isEmpty())
                server.wait();

            return server.pop();
        }
    }

    // ===========================================================================
    /**
     * Reads and returns a String. This method blocks until a String is available.
     *
     * @return The string to read.
     * @throws InterruptedException If a problem occurs.
     */
    public static String readFromClient() throws InterruptedException
    {
        synchronized (client)
        {
            while (client.isEmpty())
                client.wait();

            return client.pop();
        }
    }
}
