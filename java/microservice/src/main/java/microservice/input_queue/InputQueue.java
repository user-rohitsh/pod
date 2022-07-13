package microservice.input_queue;

import microservice.ser_des.SerDes;

import java.util.function.Consumer;

public interface InputQueue <M> {
    void Subscribe(Consumer<M> onRead, SerDes<M> deserialzer);
}
