class MockMongo(object):
    def __init__(self, config: {}):
        self.__config = config
        pass

    async def get_all_documents(self, db_name: str, collection: str):
        return [
            {"expiry": "19/05/2023", "option_id": "id1", "strike_price": 100, "type": "CALL", "underlying_id": "MSFT"},
            {"expiry": "19/06/2023", "option_id": "id2", "strike_price": 100, "type": "CALL", "underlying_id": "MSFT"},
            {"expiry": "19/05/2023", "option_id": "id3", "strike_price": 100, "type": "CALL", "underlying_id": "AAPL"},
            {"expiry": "19/05/2023", "option_id": "id4", "strike_price": 100, "type": "CALL", "underlying_id": "AAPL"}
        ]

    async def insert_all(self, db_name: str, collection: str, documents: list[dict]):
        pass
