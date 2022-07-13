package microservice;

import microservice.input_queue.InputQueue;
import microservice.output_queue.OutputQueue;
import microservice.ser_des.SerDes;
import sun.jvm.hotspot.runtime.Bytes;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

public class MemoryMappedQueue<M> implements InputQueue<M>, OutputQueue<M> {

    private MappedByteBuffer buffer;
    private final String file_name;

    public MemoryMappedQueue(String file_name) {
        this.file_name = file_name;

        //int file_d = FileChannel.open(file_name,)
    }

    public Bytes[] readFromFile() {
        return null;
    }

    @Override
    public void Subscribe(Consumer<M> onRead, SerDes<M> inputSerdes) {
        while (true) {
            onRead.accept(inputSerdes.fromBytes(readFromFile()));
        }
    }

    @Override
    public void write(M message, SerDes<M> outputSerdes) {
        outputSerdes.toBytes(message);
    }
}
