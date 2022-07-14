package utilities;

import sun.jvm.hotspot.runtime.Bytes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryMappedFile {

    private MappedByteBuffer buffer;

    public MemoryMappedFile(MappedByteBuffer buffer,
                            String file_name,
                            FileChannel.MapMode mode,
                            long start_pos, long size) throws IOException {
        this.buffer = buffer;

        try (FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel()) {
            buffer = channel.map(mode, start_pos, size);
        }
    }

    void write(Bytes[] bytes, long size )
    {

    }
}
