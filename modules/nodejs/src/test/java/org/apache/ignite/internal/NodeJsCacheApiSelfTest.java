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

/**
 * Test node js client put/get.
 */
public class NodeJsCacheApiSelfTest extends NodeJsAbstractTest {
    /** Constructor. */
    public NodeJsCacheApiSelfTest() {
        super("test-cache-api.js");
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGrid(0);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        grid(0).cache(NodeJsAbstractTest.CACHE_NAME).removeAll();

        assertNull(grid(0).cache(NodeJsAbstractTest.CACHE_NAME).get("key"));
    }

    /**
     * @throws Exception If failed.
     */
    public void testPutGet() throws Exception {
        runJsScript("testPutGet");
    }

    /**
     * @throws Exception If failed.
     */
    public void testIncorrectCache() throws Exception {
        runJsScript("testIncorrectCacheName");
    }

    /**
     * @throws Exception If failed.
     */
    public void testRemove() throws Exception {
        runJsScript("testRemove");
    }

    /**
     * @throws Exception If failed.
     */
    public void testRemoveNoKey() throws Exception {
        runJsScript("testRemoveNoKey");
    }
}
