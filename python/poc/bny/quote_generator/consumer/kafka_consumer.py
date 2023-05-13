import asyncio
import json
import logging

from aiokafka import AIOKafkaConsumer, ConsumerRecord
from confluent_kafka import Message
from kafka import TopicPartition

from quote_generator.config import app_config


def deserializer(serialized):
    return json.loads(serialized)


class KafkaConsumer(object):
    def __init__(self):
        section = "KAFKA_CONSUMER"
        self.__consumer: AIOKafkaConsumer = AIOKafkaConsumer(
            app_config.config.get(section, "topic"),
            bootstrap_servers=app_config.config.get(section, "bootstrap.servers"),
            group_id=app_config.config.get(section, "group.id"),
            value_deserializer=deserializer,
            auto_offset_reset=app_config.config.get(section, "auto.offset.reset")
        )

    async def start(self):
        await self.__consumer.start()

    async def stop(self):
        await self.__consumer.stop()

    def seek(self, offset=0):
        partitions: set[TopicPartition] = self.__consumer.assignment()
        for tp in partitions:
            self.__consumer.seek(tp, offset)

    async def consume(self, processor):
        while True:
            data: ConsumerRecord = await self.__consumer.getone()
            msg = data.value
            logging.info("Message received from kafka {}".format(msg))
            asyncio.create_task(processor(msg))
