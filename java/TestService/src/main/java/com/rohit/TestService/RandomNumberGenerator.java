package com.rohit.TestService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
public class RandomNumberGenerator {

    @Autowired
    KafkaTemplate<String,String> kafka_template;
    Random random = new Random(100);
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(fixedRate = 5000)
    public void task() {
        logger.info("Sending random number to kafka");
        kafka_template.send("TestTopic", "Generate - " + random.nextInt());
    }

    @KafkaListener(topics = "ReplyTopic", groupId = "1")
    void onMessage(ConsumerRecord<String, String> message)
    {
        logger.info("Received remply from service 1  " + message.toString());
    }

}
