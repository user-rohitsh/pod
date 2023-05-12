from abc import ABC, abstractmethod
from typing import Callable

Callback = Callable[[dict], None]


class AbstractWebSocketClient(ABC):

    @abstractmethod
    async def initialize(self, url: str, callback: Callback):
        pass

    @abstractmethod
    async def send(self, data: str):
        pass

    @abstractmethod
    async def recv(self):
        pass
