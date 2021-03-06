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

package org.apache.ignite.internal.processors.cache.query;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.marshaller.*;
import org.apache.ignite.plugin.extensions.communication.*;

import java.nio.*;

/**
 * Query.
 */
public class GridCacheSqlQuery implements Message {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    public static final Object[] EMPTY_PARAMS = {};

    /** */
    private String alias;

    /** */
    @GridToStringInclude
    private String qry;

    /** */
    @GridToStringInclude
    @GridDirectTransient
    private Object[] params;

    /** */
    private byte[] paramsBytes;

    /**
     * For {@link Message}.
     */
    public GridCacheSqlQuery() {
        // No-op.
    }

    /**
     * @param alias Alias.
     * @param qry Query.
     * @param params Query parameters.
     */
    public GridCacheSqlQuery(String alias, String qry, Object[] params) {
        A.ensure(!F.isEmpty(qry), "qry must not be empty");

        this.alias = alias;
        this.qry = qry;

        this.params = F.isEmpty(params) ? EMPTY_PARAMS : params;
    }

    /**
     * @return Alias.
     */
    public String alias() {
        return alias;
    }

    /**
     * @return Query.
     */
    public String query() {
        return qry;
    }

    /**
     * @return Parameters.
     */
    public Object[] parameters() {
        return params;
    }

    /**
     * @param m Marshaller.
     * @throws IgniteCheckedException If failed.
     */
    public void marshallParams(Marshaller m) throws IgniteCheckedException {
        if (paramsBytes != null)
            return;

        assert params != null;

        paramsBytes = m.marshal(params);
    }

    /**
     * @param m Marshaller.
     * @throws IgniteCheckedException If failed.
     */
    public void unmarshallParams(Marshaller m) throws IgniteCheckedException {
        if (params != null)
            return;

        assert paramsBytes != null;

        params = m.unmarshal(paramsBytes, null);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheSqlQuery.class, this);
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 0:
                if (!writer.writeString("alias", alias))
                    return false;

                writer.incrementState();

            case 1:
                if (!writer.writeByteArray("paramsBytes", paramsBytes))
                    return false;

                writer.incrementState();

            case 2:
                if (!writer.writeString("qry", qry))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        switch (reader.state()) {
            case 0:
                alias = reader.readString("alias");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 1:
                paramsBytes = reader.readByteArray("paramsBytes");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 2:
                qry = reader.readString("qry");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 112;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 3;
    }
}
