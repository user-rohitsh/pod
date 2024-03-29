import asyncio
import json
import logging

from aiokafka import AIOKafkaConsumer, ConsumerRecord
from kafka.structs import TopicPartition


def deserializer(serialized):
    return json.loads(serialized)


class KafkaConsumer(object):
    def __init__(self, config):
        self.__consumer: AIOKafkaConsumer = AIOKafkaConsumer(
            config["KAFKA_CONSUMER"]["topic"],
            bootstrap_servers=config["KAFKA_CONSUMER"]["bootstrap.servers"],
            group_id=config["KAFKA_CONSUMER"]["group.id"],
            value_deserializer=deserializer,
            auto_offset_reset=config["KAFKA_CONSUMER"]["auto.offset.reset"]
        )

    async def __aenter__(self):
        await self.start()
        return self

    async def __aexit__(self, exc_type, exc, tb):
        if exc_type:
            logging.error(exc)
        await self.stop()
        return self

    async def start(self):
        if self.__consumer:
            await self.__consumer.start()

    async def stop(self):
        if self.__consumer:
            await self.__consumer.stop()

    def print_partitions(self):
        partitions: set[TopicPartition] = self.__consumer.assignment()
        for tp in partitions:
            logging.info("Assigned Partition id {}".format(tp.partition))

    def seek(self, offset=0):
        partitions: set[TopicPartition] = self.__consumer.assignment()
        for tp in partitions:
            self.__consumer.seek(tp, offset)

    async def consume(self, processor):
        while True:
            logging.info("polling kafka")
            data = await self.__consumer.getmany(timeout_ms=600000)
            records: list[ConsumerRecord]
            for tp, records in data.items():
                topic = tp.topic
                partition = tp.partition
                logging.info("Messages received from kafka  {}".format(len(records)))
                msgs = []
                for record in records:
                    msg = record.value
                    msg["timestamp"] = record.timestamp
                    msgs.append(msg)

                asyncio.create_task(processor(msgs))
