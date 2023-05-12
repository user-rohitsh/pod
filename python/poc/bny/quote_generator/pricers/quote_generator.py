import asyncio
import csv
import logging

from quote_generator.pricers.utils import *
from quote_generator.pricers.utils import Option, make_request
from quote_generator.websocket_client.abstract_client import AbstractWebSocketClient


class QuoteGenerator(object):

    def __init__(self, client_impl_class: type[AbstractWebSocketClient], test_data=None):

        self.all_options: dict[str, Option] = {}
        self.underlying_to_options: dict[str, list[str]] = {}
        self.in_progress: dict[str:bool] = {}

        if test_data is not None:
            self.read(test_data)
        else:
            with open('config/options.txt', 'r') as f:
                self.read(f)

        self.__client = client_impl_class()

    def read(self, data):
        reader = csv.reader(data)
        for row in reader:
            option: Option = Option(row[0], row[1], float(row[2]), row[3], row[4])
            self.all_options[row[0]] = option
            self.underlying_to_options.setdefault(option.und, []).append(option.id)

    async def initialize(self, url):
        await  self.__client.initialize(url, self.on_reply)

    async def quote_generate(self, underlying: str, spot: float, tolerance: float):

        if self.in_progress.get(underlying):
            logging.info("skipping underlying {} as quote request is in progress")
            return

        options_ids_for_this_und: list[str] = self.underlying_to_options.get(underlying)

        # price each option
        for option_id in options_ids_for_this_und:
            option = self.all_options[option_id]
            nearest = find_nearest_spot_in_vector(option.value_vector, spot)

            if nearest is None or abs(spot - nearest.spot) > tolerance:
                # create a task to generate quote
                task_name = "{}.{}.{}.{}".format(option.und, option.type, option.expiry, option.strike)
                asyncio.create_task(self.call_quant_service_to_price(option, spot), name=task_name)
                logging.info("sent price request to quants {} {} {}".format(option.und, option.id, spot))
            else:
                interpolated_value = nearest.value + (spot - nearest.spot) * nearest.beta
                logging.info("interpolated prices{} {} {} {}".format(option.und, option.id, spot,interpolated_value))
                QuoteGenerator.quote(option, interpolated_value)

    async def call_quant_service_to_price(self, option, spot: float):
        request = make_request(option, spot)
        self.in_progress[option.und] = True
        await self.__client.send(request)

    @classmethod
    def quote(cls, option: Option, value: float):
        bid = value - 0.05
        ask = value + 0.05
        option_name = "{}|{}|{}|{}".format(option.und, option.type, option.expiry, option.strike)
        logging.info("Generating quote from task = {} :: option = {} bid = {}, ask = {}".format(
            asyncio.current_task().get_name(),
            option_name,
            bid,
            ask))
        # sendQuoteToExchange(option_id, bid,ask) ## not blocking

    def on_reply(self, message: {}):
        try:
            option_id = message["option_id"]
            value = float(message["option_value"])
            spot = float(message["spot"])
            beta = float(message["beta"])
            option: Option = self.all_options[option_id]

            logging.info("received price response from quants {} {} {}".format(option.und, option.id, spot))

            fair_value = Option.FairValue(option, spot, value, beta)
            option.value_vector.append(fair_value)
            option.value_vector.sort(key=lambda v: v.spot)

            QuoteGenerator.quote(option, value)
            self.in_progress[option.und] = False

        except Exception as ex:
            pass
        return
