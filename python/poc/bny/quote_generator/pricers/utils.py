import json
import sys


class Option(object):
    class FairValue(object):

        def __init__(self, option: "Option", spot: float, value: float, beta: float):
            self.spot = spot
            self.value = value
            self.beta = beta
            self.option = option

        def __eq__(self, other):
            if not isinstance(other, Option.FairValue):
                # don't attempt to compare against unrelated types
                return NotImplemented

            return self.spot == other.spot and self.value == other.value and self.beta == other.beta

        def __str__(self) -> str:
            return "Fair Value: {}.{}.{}".format(self.spot, self.value, self.beta)

    def __init__(self, id: str, und: str, strike: float, expiry, type: str):
        self.id = id
        self.und = und
        self.strike = strike
        self.type = type
        self.expiry = expiry
        self.value_vector: list[Option.FairValue] = []


def find_nearest_spot_in_vector(prices: list[Option.FairValue], spot_to_search: float):
    min = sys.float_info.max
    nearest: Option.FairValue = None
    for price in prices:
        diff = abs(spot_to_search - price.spot)
        if diff < min:
            nearest = price
            min = diff
    return nearest


def make_request(option: Option, spot: float):
    s = {"option_id": option.id, "spot": str(spot), "underlying_id": option.und}
    return json.dumps(s)


def option_from_mongo_document(doc):
    return Option(
        doc.get("option_id"),
        doc.get("underlying_id"),
        float(doc.get("strike_price")),
        doc.get("expiry"),
        doc.get("type")
    )
