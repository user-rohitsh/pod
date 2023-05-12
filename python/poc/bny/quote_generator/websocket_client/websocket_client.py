import asyncio
import json

import websockets

from quote_generator.websocket_client.abstract_client import AbstractWebSocketClient, Callback


class WebSocketClient(AbstractWebSocketClient):
    def __init__(self):
        self.sock: websockets.WebSocketClientProtocol = None
        self.callback = None
        self.sock: websockets.WebSocketClientProtocol
        self.__loop = asyncio.get_running_loop()

    async def initialize(self, url: str, callback: Callback):
        self.callback = callback
        self.sock = await websockets.connect(url)
        asyncio.create_task(self.recv())

    async def send(self, data: str):
        try:
            message = json.loads(data)
            data_new = json.dumps(message)
        except Exception as ex:
            return

        await self.sock.send(data_new)

    async def recv(self):
        while True:
            reply_str = await self.sock.recv()
            try:
                reply: {} = json.loads(reply_str)
                self.callback(reply)
            except Exception:
                WebSocketClient.error()

    @classmethod
    def error(cls):
        print("Error in request")
