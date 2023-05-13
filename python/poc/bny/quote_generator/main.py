import asyncio
import logging

from consumer.kafka_consumer import KafkaConsumer
from pricers.quote_generator import QuoteGenerator
from quote_generator.config import app_config
from quote_generator.websocket_client.websocket_client import WebSocketClient


async def process_message(generator: QuoteGenerator, message: dict):
    spot: float = float(message.get("spot"))
    await generator.quote_generate(message.get("und"), spot, 0.06)


async def main():
    kafka_consumer: KafkaConsumer = KafkaConsumer()
    try:
        await kafka_consumer.start()
        kafka_consumer.seek(0)

        generator = QuoteGenerator(WebSocketClient)
        quant_url = app_config.config.get("QUANT", "quant.url")
        await generator.initialize(quant_url)

        await kafka_consumer.consume(lambda message: process_message(generator, message))
    except Exception as ex:
        await kafka_consumer.stop()
        logging.error(ex)
        pass


if __name__ == '__main__':
    logging.basicConfig(filename="poc_logs.log",
                        filemode='w',
                        format='%(asctime)s,%(thread)d %(message)s',
                        datefmt='%H:%M:%S',
                        level=logging.INFO)

    asyncio.run(main())