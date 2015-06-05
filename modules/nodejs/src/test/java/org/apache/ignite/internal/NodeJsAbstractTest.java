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
import org.apache.ignite.internal.util.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.testframework.junits.common.*;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * Abstract class for Node JS testing.
 */
public class NodeJsAbstractTest extends GridCommonAbstractTest {
    /** Cache name. */
    public static final String CACHE_NAME = "mycache";

    /** Failed message. */
    public static final String SCRIPT_FAILED = "node js test failed:";

    /** Ok message. */
    public static final String SCRIPT_FINISHED = "node js test finished.";

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

    /**
     * @param path Path to script.
     * @throws Exception If script failed.
     */
    protected void runJsScript(String path) throws Exception {
        final CountDownLatch readyLatch = new CountDownLatch(1);

        GridJavaProcess proc = null;

        final List<String> errors = new ArrayList<>();

        List<String> cmd = new ArrayList<>();

        cmd.add("node");

        cmd.add(path);

        Map<String, String> env = new HashMap<>();

        env.put("IGNITE_HOME", IgniteUtils.getIgniteHome());

        try {
            proc = GridJavaProcess.exec(
                cmd,
                env,
                log,
                new CI1<String>() {
                    @Override public void apply(String s) {
                        info("Node js: " + s);

                        s = s.toLowerCase();

                        if (s.contains(SCRIPT_FINISHED))
                            readyLatch.countDown();

                        if (/*s.contains("error") || s.contains("fail") || */s.contains(SCRIPT_FAILED)) {
                            errors.add(s);

                            readyLatch.countDown();
                        }
                    }
                },
                null
            );

            assertTrue(readyLatch.await(60, SECONDS));

            assertEquals(errors.toString(), 0, errors.size());

            proc.getProcess().waitFor();
        }
        finally {
            if (proc != null)
                proc.killProcess();
        }
    }
}
