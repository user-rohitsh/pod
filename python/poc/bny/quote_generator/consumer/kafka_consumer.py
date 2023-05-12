import asyncio
import logging

from confluent_kafka import Consumer, Message, TopicPartition

from quote_generator.config import app_config
from quote_generator.consumer.abstract_consumer import AbstractConsumer


class KafkaConsumer(AbstractConsumer):
    def __init__(self):
        section = "KAFKA_CONSUMER"
        conf = {
            "bootstrap.servers": app_config.config.get(section, "bootstrap.servers"),
            "group.id": app_config.config.get(section, "group.id"),
            "enable.auto.commit": app_config.config.getboolean(section, "enable.auto.commit"),
            "auto.offset.reset": app_config.config.get(section, "auto.offset.reset")}

        self.__consumer: Consumer = Consumer(conf)
        self.__loop = asyncio.get_running_loop()

    def subscribe(self, topics):
        self.__consumer.subscribe(topics)

    def seek(self, topic, partition=0, offset=0):
        tp = TopicPartition(topic=topic, partition=partition, offset=offset)
        self.__consumer.assign([tp])
        self.__consumer.seek(tp)

    def poll(self):
        message: Message = self.__consumer.poll(10)

        if message is not None:
            logging.info("Message received from kafka {}".format(message.value()))
        else:
            logging.info("Message received from kafka {}".format("No Message"))
        return message
