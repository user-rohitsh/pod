package microservice;

import microservice.Persitence.Persistence;
import microservice.input_queue.InputQueue;
import microservice.output_queue.OutputQueue;
import microservice.ser_des.SerDes;

public class Microservice<Input, Output> {
    private final InputQueue<Input> input_queue;
    private final OutputQueue<Output> output_queue;
    private final Persistence persistence;

    public Microservice(InputQueue<Input> input_queue, OutputQueue<Output> output_queue,
                        Persistence persistence) {
        this.input_queue = input_queue;
        this.output_queue = output_queue;
        this.persistence = persistence;
    }

    public void start(SerDes<Input> serdes) {
        input_queue.Subscribe(this::onRead, serdes);
    }

    void onRead(Input message) {
        // Service loop
    }
}
