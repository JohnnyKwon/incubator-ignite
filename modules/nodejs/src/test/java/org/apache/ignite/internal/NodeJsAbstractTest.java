/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal;

import org.apache.ignite.cache.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.testframework.junits.common.*;

/**
 * Abstract class for Node JS testing.
 */
public class NodeJsAbstractTest extends GridCommonAbstractTest {
    /** Cache name. */
    public static final String CACHE_NAME = "mycache";

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);
        cfg.setCacheConfiguration(cacheConfiguration());

        ConnectorConfiguration conCfg = new ConnectorConfiguration();

        conCfg.setJettyPath(getNodeJsTestDir() + "rest-jetty.xml");

        cfg.setConnectorConfiguration(conCfg);

        return cfg;
    }

    /**
     * @return Cache configuration.
     */
    protected CacheConfiguration cacheConfiguration() {
        CacheConfiguration ccfg = new CacheConfiguration();

        ccfg.setName(CACHE_NAME);
        ccfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);

        return ccfg;
    }

    /**
     * @return Node js test dir.
     */
    protected String getNodeJsTestDir() {
        String sep = System.getProperty("file.separator");

        return U.getIgniteHome() +
            sep + "modules" +
            sep + "nodejs" +
            sep + "src" +
            sep + "test" +
            sep + "nodejs" + sep;
    }
}
