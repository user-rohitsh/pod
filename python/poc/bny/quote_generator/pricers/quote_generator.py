import asyncio
import logging

from quote_generator.config import app_config
from quote_generator.mongo.mongodb import Mongo
from quote_generator.pricers.utils import *
from quote_generator.pricers.utils import Option, make_request
from quote_generator.websocket_client.abstract_client import AbstractWebSocketClient


class QuoteGenerator(object):

    def __init__(self, client_impl_class: type[AbstractWebSocketClient], options: list[Option] = None):
        self.all_options: dict[str, Option] = {}
        self.underlying_to_options: dict[str, list[str]] = {}
        self.in_progress: dict[str:bool] = {}

        if options is not None:
            self.read(options)
        else:
            section = "MONGODB"
            url = app_config.config.get(section, "mongodb.url")
            db_name = app_config.config.get(section, "mongodb.dbname")
            collection = app_config.config.get(section, "mongodb.collection")
            self.mongo = Mongo(url)
            mongo_documents = self.mongo.read_all_documents(db_name, collection)
            options = [option_from_mongo_document(document) for document in mongo_documents]
            self.read(options)

        self.__client = client_impl_class()

    def read(self, options):
        for option in options:
            self.all_options[option.id] = option
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
                self.in_progress[option.und] = True
                asyncio.create_task(self.call_quant_service_to_price(option, spot), name=task_name)
                logging.info("sent price request to quants {} {} {}".format(option.und, option.id, spot))
            else:
                interpolated_value = nearest.value + (spot - nearest.spot) * nearest.beta
                logging.info("interpolated prices{} {} {} {}".format(option.und, option.id, spot, interpolated_value))
                self.quote(option, interpolated_value)

    async def call_quant_service_to_price(self, option, spot: float):
        request = make_request(option, spot)
        await self.__client.send(request)

    def quote(self, option: Option, value: float):
        bid = value - 0.05
        ask = value + 0.05
        option_name = "{}|{}|{}|{}".format(option.und, option.type, option.expiry, option.strike)
        logging.info("Generating quote from task = {} :: option = {} bid = {}, ask = {}".format(
            asyncio.current_task().get_name(),
            option_name,
            bid,
            ask))
        # sendQuoteToExchange(option_id, bid,ask) ## not blocking
        # save quotes in mongo DB

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

            self.quote(option, value)
            self.in_progress[option.und] = False

        except Exception as ex:
            logging.error("Error in parsing reply")
        return
