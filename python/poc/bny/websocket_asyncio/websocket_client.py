import asyncio
import json
import logging
from typing import Callable

import websockets
from websocket import WebSocketException
from websockets.exceptions import ConnectionClosedError

Callback = Callable[[dict], None]


class WebSocketClient():
    def __init__(self, config: {}):
        self.__recv_task = None
        self.__is_active = False
        self.sock: websockets.WebSocketClientProtocol = None
        self.callback = None
        self.sock: websockets.WebSocketClientProtocol
        self.__loop = asyncio.get_running_loop()
        self.__url = config["QUANT"]["quant.url"]

    async def initialize(self, callback: Callback):
        self.callback = callback
        await self.connect()
        if self.__is_active:
            self.__recv_task = asyncio.create_task(self.recv())

    async def close(self):
        if self.__recv_task:
            self.__recv_task.cancel()
        if self.__is_active:
            await self.sock.close()

    async def send(self, data: str):

        if not self.__is_active:
            logging.error("connection lost --ignoring send")
            return

        try:
            message = json.loads(data)
            data_new = json.dumps(message)
            await self.sock.send(data_new)
        except WebSocketException as ex:
            self.__is_active = False
            logging.error("connection lost --ignoring send")
            return
        except Exception:
            logging.error("Error sending data - ignoring send")
            return

    async def recv(self):
        while True:
            reply = {}
            try:
                reply_str = await self.sock.recv()
                reply = json.loads(reply_str)
            except (ConnectionClosedError, ConnectionAbortedError, ConnectionResetError) as ex:
                logging.error("connection lost --trying reconnect")
                self.__is_active = False
                await self.connect()
            except Exception as ex:
                logging.error("Error reading reply from quant service --aborting recv")

            self.callback(reply)

    async def connect(self):
        while True:
            try:
                self.sock = await websockets.connect(self.__url)
                self.__is_active = True
                logging.info("connected")
                return
            except (ConnectionRefusedError, ConnectionError, ConnectionAbortedError) as ex:
                logging.error("Cannot create connection to quant service - retrying after 1 sec {}".format(ex))
            await asyncio.sleep(1)
