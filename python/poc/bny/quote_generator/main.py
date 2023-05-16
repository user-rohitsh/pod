import asyncio
import logging
import sys
from multiprocessing import Process

from dependency_injector import containers, providers

from kafka_asyncio.kafka_consumer import KafkaConsumer
from mongo_asyncio.mongodb import Mongo
from pricers.quote_generator import QuoteGenerator
from websocket_asyncio.websocket_client import WebSocketClient


class Container(containers.DeclarativeContainer):
    config = providers.Configuration()
    config.from_ini("config/bny.config")

    web_sock_client = providers.Factory(
        WebSocketClient,
        config=config
    )

    mongo_client = providers.Factory(
        Mongo,
        config=config
    )

    kafka_consumer = providers.Factory(
        KafkaConsumer,
        config=config
    )


async def process_message(generator: QuoteGenerator, messages: list[dict]):
    logging.info("processing quote for {}".format(' '.join([elem["und"] for elem in messages])))
    await generator.quote_generate(messages)


async def poc_main(container):
    mongo: Mongo = container.mongo_client()
    sock_client: WebSocketClient = container.web_sock_client()
    config = container.config()

    async with container.kafka_consumer() as kafka_consumer, \
            QuoteGenerator(sock_client, mongo, config=config) as generator:
        await kafka_consumer.consume(lambda messages: process_message(generator, messages))


def start(instance_name: str):
    logging.basicConfig(filename="{}.log".format(instance_name),
                        filemode='w',
                        format='%(asctime)s,%(thread)d %(message)s',
                        datefmt='%H:%M:%S',
                        level=logging.INFO)

    logging.getLogger("consumer").setLevel(logging.INFO)

    container = Container()
    asyncio.run(poc_main(container))
    logging.info("Exiting {} due to error".format(instance_name))
    sys.exit(-1)


if __name__ == '__main__':
    instance_1 = Process(target=start, args=('instance.1',))
    instance_2 = Process(target=start, args=('instance.2',))

    instance_1.start()
    instance_2.start()

    instance_1.join()
    instance_2.join()
