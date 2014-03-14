/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence_jpa_hibernate;

import java.io.File;

import org.gluewine.junit.GluewineTestService;
import org.gluewine.persistence_jpa_hibernate.impl.TestSessionProvider;

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
    protected HibernateSessionProvider getProvider()
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
