import asyncio
import logging
from multiprocessing import Process

from dependency_injector import containers, providers
from dependency_injector.wiring import Provide, inject

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


@inject
async def poc_main(
        kafka_consumer: KafkaConsumer = Provide[Container.kafka_consumer],
        mongo: Mongo = Provide[Container.mongo_client],
        sock_client: WebSocketClient = Provide[Container.web_sock_client],
        config=Provide[Container.config]):
    try:
        await kafka_consumer.start()
        #kafka_consumer.print_partitions()
        generator = QuoteGenerator(sock_client, mongo, config=config)
        await generator.initialize()

        #kafka_consumer.seek(0)  # for poc - resetting offset to 0
        await kafka_consumer.consume(lambda message: process_message(generator, message))
    except Exception as ex:
        await kafka_consumer.stop()
        logging.error(ex)
        pass


def start(instance_name: str):
    logging.basicConfig(filename="{}.log".format(instance_name),
                        filemode='w',
                        format='%(asctime)s,%(thread)d %(message)s',
                        datefmt='%H:%M:%S',
                        level=logging.INFO)

    logging.getLogger("consumer").setLevel(logging.INFO)

    container = Container()
    container.wire(modules=[__name__])
    asyncio.run(poc_main())


if __name__ == '__main__':
    instance_1 = Process(target=start, args=('instance.1',))
    instance_2 = Process(target=start, args=('instance.2',))

    instance_1.start()
    instance_2.start()

    instance_1.join()
    instance_2.join()
