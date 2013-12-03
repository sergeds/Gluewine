package org.gluewine.persistence;

import java.io.File;

import org.gluewine.junit.GluewineTestService;
import org.gluewine.persistence.impl.TestSessionProvider;

/**
 * Test service extension to be used in projects using the Hibernate modules.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class GluewineHibernateTestService extends GluewineTestService
{
    // ===========================================================================
    /**
     * The current provider instance.
     */
    private TestSessionProvider provider = null;


    // ===========================================================================
    /**
     * Returns the array of entities to be used in this test.
     *
     * @return The entities to use.
     */
    protected abstract Class<?>[] getEntities();

    // ===========================================================================
    /**
     * Returns the provider to be used.
     *
     * @return The provider to use.
     */
    protected SessionProvider getProvider()
    {
        if (provider == null)
        {
            String cfgDir = System.getProperty("cfg.dir");
            if (cfgDir != null)
            {
                File f = new File(cfgDir, "test_hibernate.properties");
                provider = new TestSessionProvider(f, getEntities());
                addService(provider);
            }

            else throw new RuntimeException("The cfg.dir is not specified as a system property!");
        }

        return provider;
    }

    // ===========================================================================
    /**
     * Closes the gluewine test environment.
     */
    public void closeGluewine()
    {
        if (provider != null)
        {
            provider.closeProvider();
        }

        super.closeGluewine();
    }
}
