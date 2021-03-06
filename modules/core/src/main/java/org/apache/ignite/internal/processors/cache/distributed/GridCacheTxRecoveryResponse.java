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

package org.apache.ignite.internal.processors.cache.distributed;

import org.apache.ignite.internal.processors.cache.version.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;

import java.io.*;
import java.nio.*;

/**
 * Transactions recovery check response.
 */
public class GridCacheTxRecoveryResponse extends GridDistributedBaseMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /** Future ID. */
    private IgniteUuid futId;

    /** Mini future ID. */
    private IgniteUuid miniId;

    /** Flag indicating if all remote transactions were prepared. */
    private boolean success;

    /**
     * Empty constructor required by {@link Externalizable}
     */
    public GridCacheTxRecoveryResponse() {
        // No-op.
    }

    /**
     * @param txId Transaction ID.
     * @param futId Future ID.
     * @param miniId Mini future ID.
     * @param success {@code True} if all remote transactions were prepared, {@code false} otherwise.
     */
    public GridCacheTxRecoveryResponse(GridCacheVersion txId,
        IgniteUuid futId,
        IgniteUuid miniId,
        boolean success)
    {
        super(txId, 0);

        this.futId = futId;
        this.miniId = miniId;
        this.success = success;
    }

    /**
     * @return Future ID.
     */
    public IgniteUuid futureId() {
        return futId;
    }

    /**
     * @return Mini future ID.
     */
    public IgniteUuid miniId() {
        return miniId;
    }

    /**
     * @return {@code True} if all remote transactions were prepared.
     */
    public boolean success() {
        return success;
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 8:
                if (!writer.writeIgniteUuid("futId", futId))
                    return false;

                writer.incrementState();

            case 9:
                if (!writer.writeIgniteUuid("miniId", miniId))
                    return false;

                writer.incrementState();

            case 10:
                if (!writer.writeBoolean("success", success))
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

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 8:
                futId = reader.readIgniteUuid("futId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 9:
                miniId = reader.readIgniteUuid("miniId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 10:
                success = reader.readBoolean("success");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 17;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 11;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheTxRecoveryResponse.class, this, "super", super.toString());
    }
}
