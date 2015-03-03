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

import org.apache.ignite.*;
import org.apache.ignite.internal.processors.cache.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.marshaller.*;
import org.jdk8.backport.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * Marshaller context implementation.
 */
public class MarshallerContextImpl implements MarshallerContext {
    /** */
    private static final String CLS_NAMES_FILE = "org/apache/ignite/internal/classnames.properties";

    /** */
    private final ConcurrentMap<Integer, String> clsNameById = new ConcurrentHashMap8<>();

    /** */
    private final CountDownLatch latch = new CountDownLatch(1);

    /** */
    private volatile GridCacheAdapter<Integer, String> cache;

    /**
     * Constructor.
     */
    MarshallerContextImpl() {
        try {
            ClassLoader ldr = getClass().getClassLoader();

            BufferedReader rdr = new BufferedReader(new InputStreamReader(ldr.getResourceAsStream(CLS_NAMES_FILE)));

            String clsName;

            while ((clsName = rdr.readLine()) != null)
                clsNameById.put(clsName.hashCode(), clsName);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to initialize marshaller context.", e);
        }
    }

    /**
     * @param ctx Kernal context.
     */
    public void onMarshallerCacheReady(GridKernalContext ctx) {
        assert ctx != null;

        cache = ctx.cache().marshallerCache();

        latch.countDown();
    }

    /** {@inheritDoc} */
    @Override public void registerClass(int id, Class cls) {
        if (clsNameById.putIfAbsent(id, cls.getName()) == null) {
            try {
                if (cache == null)
                    U.awaitQuiet(latch);

                String old = cache.putIfAbsent(id, cls.getName());

                if (old != null && !old.equals(cls.getName()))
                    throw new IgniteException("Type ID collision occurred in OptimizedMarshaller. Use " +
                        "OptimizedMarshallerIdMapper to resolve it [id=" + id + ", clsName1=" + cls.getName() +
                        "clsName2=" + old + ']');
            }
            catch (IgniteCheckedException e) {
                throw U.convertException(e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public Class className(int id, ClassLoader ldr) throws ClassNotFoundException {
        String clsName = clsNameById.get(id);

        if (clsName == null) {
            if (cache == null)
                U.awaitQuiet(latch);

            try {
                clsName = cache.get(id);
            }
            catch (IgniteCheckedException e) {
                throw U.convertException(e);
            }

            assert clsName != null : id;

            String old = clsNameById.putIfAbsent(id, clsName);

            if (old != null)
                clsName = old;
        }

        return U.forName(clsName, ldr);
    }
}