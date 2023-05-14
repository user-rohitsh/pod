import asyncio
import datetime
import logging
from itertools import groupby

from quote_generator.pricers.utils import *
from quote_generator.pricers.utils import Option, make_request, find_nearest_spot_in_vector


class QuoteGenerator(object):

    def __init__(self, sock_client, mongo, config: {}):
        self.all_options: dict[str, Option] = {}
        self.underlying_to_options: dict[str, list[str]] = {}
        self.in_progress: dict[str:bool] = {}
        self.__config = config
        self.mongo = mongo
        self.__web_socket_client = sock_client
        self.__tolerance = float(self.__config["QUANT"]["quant.tolerance"])

    def read(self, options):
        for option in options:
            self.all_options[option.id] = option
            self.underlying_to_options.setdefault(option.und, []).append(option.id)

    async def initialize(self):
        await self.__web_socket_client.initialize(self.on_reply)
        db_name = self.__config["MONGODB"]["mongodb.dbname"]
        collection = self.__config["MONGODB"]["mongodb.collection"]
        mongo_documents = await self.mongo.get_all_documents(db_name, collection)
        options = [option_from_mongo_document(document) for document in mongo_documents]
        self.read(options)

    async def quote_generate(self, messages: list[dict]):

        messages.sort(key=lambda msg: (msg.get("und"), msg.get("timestamp")))
        collated = [list(g).pop() for k, g in groupby(messages, lambda msg: msg.get("und"))]

        for m in collated:
            spot = float(m["spot"])
            underlying = m["und"]
            await self.interpolate_or_price(underlying, spot)

    async def interpolate_or_price(self, underlying: str, spot: float):

        if self.in_progress.get(underlying):
            logging.info("skipping underlying {} as quote request is in progress")
            return

        options_ids_for_this_und: list[str] = self.underlying_to_options.get(underlying, [])

        # price each option
        for option_id in options_ids_for_this_und:
            option = self.all_options[option_id]
            nearest = find_nearest_spot_in_vector(option.value_vector, spot)

            if nearest is None or abs(spot - nearest.spot) > self.__tolerance:
                # create a task to generate quote
                task_name = "{}.{}.{}.{}".format(option.und, option.type, option.expiry, option.strike)
                self.in_progress[option.und] = True
                asyncio.create_task(self.call_quant_service_to_price(option, spot), name=task_name)
                logging.info("sent price request to quants {} {} {}".format(option.und, option.id, spot))
            else:
                interpolated_value = nearest.value + (spot - nearest.spot) * nearest.beta
                logging.info("interpolated price {} {} {} {}".format(option.und, option.id, spot, interpolated_value))
                self.quote(option, interpolated_value)

    async def call_quant_service_to_price(self, option, spot: float):
        request = make_request(option, spot)
        await self.__web_socket_client.send(request)

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
        ct = datetime.datetime.now()
        ts = ct.timestamp()
        asyncio.create_task(self.mongo.insert_all("POC", "quotes", [
            {
                "time": ts,
                "option_id": option.id,
                "underlying": option.und,
                "bid": bid,
                "ask": ask
            }
        ]))

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

        except Exception:
            logging.error("Error in parsing reply")
        return
