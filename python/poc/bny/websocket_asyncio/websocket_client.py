import asyncio
import json
import logging
import sys
from typing import Callable

import websockets

Callback = Callable[[dict], None]


class WebSocketClient():
    def __init__(self, config: {}):
        self.sock: websockets.WebSocketClientProtocol = None
        self.callback = None
        self.sock: websockets.WebSocketClientProtocol
        self.__loop = asyncio.get_running_loop()
        self.__url = config["QUANT"]["quant.url"]
        pass

    async def initialize(self, callback: Callback):
        self.callback = callback
        try:
            self.sock = await websockets.connect(self.__url)
            if self.sock is None:
                logging.error("Cannot create connection to quant service - Shutting Down")
                sys.exit(-1)
        except Exception as ex:
            logging.error("Cannot create connection to quant service - Shutting Down {}".format(ex))
            raise ex

        asyncio.create_task(self.recv())

    async def send(self, data: str):
        try:
            message = json.loads(data)
            data_new = json.dumps(message)
        except Exception as ex:
            logging.error("Error sending data to quant service --aborting send")
            return

        await self.sock.send(data_new)

    async def recv(self):
        while True:
            reply = {}
            reply_str = await self.sock.recv()
            try:
                reply = json.loads(reply_str)
            except Exception:
                logging.error("Error reading reply from quant service --aborting recv")

            self.callback(reply)
