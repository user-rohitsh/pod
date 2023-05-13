import logging
from unittest import IsolatedAsyncioTestCase

from quote_generator.pricers.quote_generator import QuoteGenerator
from quote_generator.pricers.utils import Option
from quote_generator.tests.unit.mock_websocket_client import MockWebSocketClient


class TestQuoteGenerator(IsolatedAsyncioTestCase):

    def setUp(self) -> None:
        logging.basicConfig(filename="poc_test.log",
                            filemode='a',
                            format='%(asctime)s,%(thread)d %(message)s',
                            datefmt='%H:%M:%S',
                            level=logging.DEBUG)

        self.options = [
            Option("id1","MSFT",100.0,"19/05/2023", "CALL"),
            Option("id2", "MSFT", 100.0, "17/06/2023", "CALL"),
            Option("id3", "AAPL", 100.0, "19/05/2023", "CALL"),
            Option("id4", "AAPL", 100.0, "17/06/2023", "CALL"),
        ]

    async def test_full_price(self):

        generator = QuoteGenerator(
            MockWebSocketClient,
            self.options
        )

        await generator.initialize("")

        msft_option_ids: list[str] = generator.underlying_to_options.get("MSFT")
        msft_options: list[Option] = [generator.all_options.get(option_id) for option_id in msft_option_ids]

        for option in msft_options:
            await generator.call_quant_service_to_price(option, 10.0)
            self.assertListEqual(option.value_vector, [Option.FairValue(option, 10.0, 10.0, 1.0)])

    async def test_linear_interpolation(self):
        generator = QuoteGenerator(
            MockWebSocketClient,
            self.options
         )

        await generator.initialize("")

        msft_option_ids: list[str] = generator.underlying_to_options.get("MSFT")
        msft_options: list[Option] = [generator.all_options.get(option_id) for option_id in msft_option_ids]

        for option in msft_options:
            await generator.call_quant_service_to_price(option, 10.0)
            await generator.call_quant_service_to_price(option, 10.1)
            await generator.call_quant_service_to_price(option, 10.2)

        for option in msft_options:
            await generator.quote_generate("MSFT", 10.04, 0.1)
            self.assertListEqual(
                option.value_vector, [
                    Option.FairValue(option, 10.0, 10.0, 1.0),
                    Option.FairValue(option, 10.1, 10.1, 1.0),
                    Option.FairValue(option, 10.2, 10.2, 1.0)
                ]
            )
