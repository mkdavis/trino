/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.orc.stream;

import io.airlift.slice.Slice;
import io.prestosql.orc.OrcCorruptionException;
import io.prestosql.orc.OrcDecompressor;
import io.prestosql.orc.checkpoint.ByteStreamCheckpoint;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.prestosql.memory.context.AggregatedMemoryContext.newSimpleAggregatedMemoryContext;
import static io.prestosql.orc.OrcDecompressor.createOrcDecompressor;
import static io.prestosql.orc.metadata.CompressionKind.SNAPPY;

public class TestByteStream
        extends AbstractTestValueStream<Byte, ByteStreamCheckpoint, ByteOutputStream, ByteInputStream>
{
    @Test
    public void testLiteral()
            throws IOException
    {
        List<List<Byte>> groups = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < 3; groupIndex++) {
            List<Byte> group = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                group.add((byte) (groupIndex * 10_000 + i));
            }
            groups.add(group);
        }
        testWriteValue(groups);
    }

    @Test
    public void testRleLong()
            throws IOException
    {
        List<List<Byte>> groups = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < 3; groupIndex++) {
            List<Byte> group = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                group.add((byte) 77);
            }
            groups.add(group);
        }
        testWriteValue(groups);
    }

    @Test
    public void testRleShort()
            throws IOException
    {
        List<List<Byte>> groups = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < 3; groupIndex++) {
            List<Byte> group = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                group.add((byte) ((groupIndex * 10_000 + i) / 13));
            }
            groups.add(group);
        }
        testWriteValue(groups);
    }

    @Override
    protected ByteOutputStream createValueOutputStream()
    {
        return new ByteOutputStream(SNAPPY, COMPRESSION_BLOCK_SIZE);
    }

    @Override
    protected void writeValue(ByteOutputStream outputStream, Byte value)
    {
        outputStream.writeByte(value);
    }

    @Override
    protected ByteInputStream createValueStream(Slice slice)
            throws OrcCorruptionException
    {
        Optional<OrcDecompressor> orcDecompressor = createOrcDecompressor(ORC_DATA_SOURCE_ID, SNAPPY, COMPRESSION_BLOCK_SIZE);
        return new ByteInputStream(new OrcInputStream(OrcChunkLoader.create(ORC_DATA_SOURCE_ID, slice, orcDecompressor, newSimpleAggregatedMemoryContext())));
    }

    @Override
    protected Byte readValue(ByteInputStream valueStream)
            throws IOException
    {
        return valueStream.next();
    }
}