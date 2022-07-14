package microservice.state_management;

import Value.Value;
import Value.ValueWrapper;
import microservice.ser_des.SerDes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class KafkaBackedState<V> implements ApplicationState<V> {

    private final SerDes<ValueWrapper<V>> ser_des;
    private final String topic;
    private final Producer<Integer, ValueWrapper<V>> producer;
    private final ProducerRecord<Integer, ValueWrapper<V>> record;
    private final KafkaConsumer<Integer, ValueWrapper<V>> consumer;

    public KafkaBackedState(SerDes<ValueWrapper<V>> ser_des, String topic) {
        this.topic = topic;
        this.ser_des = ser_des;

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", ser_des.getClass().getName());
        producer = new KafkaProducer<>(props);
        record = new ProducerRecord(
                topic,
                ProcessHandle.current().pid(),
                new ValueWrapper(ser_des) {
                });

        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    @Override
    public void save(V message) {
        record.value().setValue(message);
        producer.send(record);
    }

    @Override
    public V retrieve() {
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<Integer, ValueWrapper<V>> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<Integer, ValueWrapper<V>> record : records) {

            }
        }
        return null;
    }

    @Override
    public long position() {
        return 0;
    }

    @Override
    public void seek(long position) {

    }

    @Override
    public void rewind(long diff) {

    }
}
