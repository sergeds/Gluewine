package org.gluewine.persistence_jpa_hibernate;

import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence_jpa.SessionProvider;
import org.gluewine.persistence_jpa.TransactionalSession;

import org.hibernate.cfg.Configuration;

/**
 * Hibernate Specific SessionProvider.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface HibernateSessionProvider extends SessionProvider
{
    // ===========================================================================
    /**
     * Returns the session to be used.
     *
     * @return The session to use.
     */
    @SuppressWarnings("unchecked")
    HibernateTransactionalSession getSession();

    /**
     * Gets the hibernate configuration. Useful if code needs the class mapping.
     *
     * @return the configuration.
     */
    Configuration getConfiguration();

    // ===========================================================================
    /**
     * Returns the session to be used and requests the callback specified to be
     * invoked when the session is committed or rolled back.
     *
     * @param callback The callback to notify.
     * @return The session to use.
     */
    <T extends TransactionalSession> T getSession(TransactionCallback callback);
}
