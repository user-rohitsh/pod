import asyncio
import json
import logging
from abc import ABC
from typing import Callable

from quote_generator.websocket_client.abstract_client import AbstractWebSocketClient, Callback


def process_request(data: str) -> dict:
    try:
        payload_dict = json.loads(data)
        option_id = payload_dict["option_id"]
        spot = payload_dict["spot"]

        reply = {
            "option_value": spot,
            "beta": "1.0",
            "option_id": option_id,
            "spot": spot
        }

    except Exception as ex:
        reply={}

    return reply


class MockWebSocketClient(AbstractWebSocketClient):
    def __init__(self):
        self.__loop = asyncio.get_running_loop()
        self.callback = None

    async def initialize(self, url: str, callback: Callback):
        self.callback = callback

    async def send(self, data: str):
        if self.callback is None:
            return

        logging.debug("send request to quant service {}", data)
        reply = process_request(data)
        if reply == "{}":
            MockWebSocketClient.error()
        else:
            self.callback(reply)

    async def recv(self):
        pass

    @classmethod
    def error(cls):
        print("Error in request")
