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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.*;
import org.apache.ignite.cache.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.managers.communication.*;
import org.apache.ignite.internal.processors.cache.distributed.near.*;
import org.apache.ignite.plugin.extensions.communication.*;
import org.apache.ignite.spi.*;
import org.apache.ignite.spi.communication.tcp.*;
import org.apache.ignite.testframework.junits.common.*;
import org.apache.ignite.transactions.*;

import java.util.*;
import java.util.concurrent.*;

import static org.apache.ignite.transactions.TransactionConcurrency.*;
import static org.apache.ignite.transactions.TransactionIsolation.*;

/**
 *
 */
public class IgniteCacheNearLockValueSelfTest extends GridCommonAbstractTest {
    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGridsMultiThreaded(2);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        super.afterTestsStopped();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        if (getTestGridName(0).equals(gridName))
            cfg.setClientMode(true);

        cfg.setCommunicationSpi(new TestCommunicationSpi());

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testDhtVersion() throws Exception {
        CacheConfiguration<Object, Object> pCfg = new CacheConfiguration<>("partitioned");

        pCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        try (IgniteCache<Object, Object> cache = ignite(0).getOrCreateCache(pCfg, new NearCacheConfiguration<>())) {
            cache.put("key1", "val1");

            for (int i = 0; i < 3; i++) {
                ((TestCommunicationSpi)ignite(0).configuration().getCommunicationSpi()).clear();
                ((TestCommunicationSpi)ignite(1).configuration().getCommunicationSpi()).clear();

                try (Transaction tx = ignite(0).transactions().txStart(PESSIMISTIC, REPEATABLE_READ)) {
                    cache.get("key1");

                    tx.commit();
                }

                TestCommunicationSpi comm = (TestCommunicationSpi)ignite(0).configuration().getCommunicationSpi();

                assertEquals(1, comm.requests().size());

                GridCacheAdapter<Object, Object> primary = ((IgniteKernal)grid(1)).internalCache("partitioned");

                GridCacheEntryEx dhtEntry = primary.peekEx(primary.context().toCacheKeyObject("key1"));

                assertNotNull(dhtEntry);

                GridNearLockRequest req = comm.requests().iterator().next();

                assertEquals(dhtEntry.version(), req.dhtVersion(0));

                // Check entry version in near cache after commit.
                GridCacheAdapter<Object, Object> near = ((IgniteKernal)grid(0)).internalCache("partitioned");

                GridNearCacheEntry nearEntry = (GridNearCacheEntry)near.peekEx(near.context().toCacheKeyObject("key1"));

                assertNotNull(nearEntry);

                assertEquals(dhtEntry.version(), nearEntry.dhtVersion());
            }
        }
    }

    /**
     *
     */
    private static class TestCommunicationSpi extends TcpCommunicationSpi {
        /** */
        private Collection<GridNearLockRequest> reqs = new ConcurrentLinkedDeque<>();

        /** {@inheritDoc} */
        @Override public void sendMessage(ClusterNode node, Message msg) throws IgniteSpiException {
            if (msg instanceof GridIoMessage) {
                GridIoMessage ioMsg = (GridIoMessage)msg;

                if (ioMsg.message() instanceof GridNearLockRequest)
                    reqs.add((GridNearLockRequest)ioMsg.message());
            }

            super.sendMessage(node, msg);
        }

        /**
         * @return Collected requests.
         */
        public Collection<GridNearLockRequest> requests() {
            return reqs;
        }

        /**
         *
         */
        public void clear() {
            reqs.clear();
        }
    }
}
