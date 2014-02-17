package org.gluewine.authentication;

/**
 * Defines authentication using userid and password.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface UseridPasswordAuthentication
{
    // ===========================================================================
    /**
     * Authenticates using the given userid and password.
     *
     * @param user The user to authenticate.
     * @param password The password of the user.
     * @return The session id.
     * @throws AuthenticationException If authentication failed.
     */
    String authenticate(String user, String password) throws AuthenticationException;
    
    //test
}
