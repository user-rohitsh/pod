import asyncio
import json
import logging

import confluent_kafka

from consumer.kafka_consumer import KafkaConsumer
from pricers.quote_generator import QuoteGenerator
from quote_generator.websocket_client.websocket_client import WebSocketClient


async def main():
    logging.basicConfig(filename="poc_logs.log",
                        filemode='w',
                        format='%(asctime)s,%(thread)d %(message)s',
                        datefmt='%H:%M:%S',
                        level=logging.INFO)

    kafka_consumer: KafkaConsumer = KafkaConsumer()
    kafka_consumer.subscribe(["spots"])

    # /testing
    kafka_consumer.seek("spots", 0, 0)

    pricer = QuoteGenerator(WebSocketClient)
    await pricer.initialize("ws://localhost:8000/quant")

    while True:
        kafka_message: confluent_kafka.Message = await asyncio.get_running_loop().run_in_executor(
            None,
            kafka_consumer.poll)
        if kafka_message is None: continue
        message: dict = json.loads(kafka_message.value())
        spot: float = float(message.get("spot"))
        await pricer.quote_generate(message.get("und"), spot, 0.06)
        await asyncio.sleep(5)


if __name__ == '__main__':
    asyncio.run(main())

    # asyncio.run(websocket_test())
