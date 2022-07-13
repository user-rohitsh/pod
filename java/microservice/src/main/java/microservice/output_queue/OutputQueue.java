package microservice.output_queue;

import microservice.ser_des.SerDes;

public interface OutputQueue<M> {
    void write(M message, SerDes<M> outputSerdes);
}
