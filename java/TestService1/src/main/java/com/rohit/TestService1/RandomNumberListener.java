package com.rohit.TestService1;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RandomNumberListener {

    @Autowired
    KafkaTemplate<String,String > template;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @KafkaListener(topics = "TestTopic", groupId = "1")
    void onMessage(ConsumerRecord<String, String> message)
    {
        logger.info("Received message " + message.toString());
        template.send("ReplyTopic" , "Reply from service 1 -- received " + message.toString());
    }
}
