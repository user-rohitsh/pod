from abc import ABC, abstractmethod


class AbstractConsumer(ABC):

    @abstractmethod
    def poll(self):
        pass
