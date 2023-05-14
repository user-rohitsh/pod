import logging

import motor
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorCollection


class Mongo(object):
    def __init__(self, config: {}):
        url = config["MONGODB"]["mongodb.url"]
        self.mongo_client: AsyncIOMotorClient = motor.motor_asyncio.AsyncIOMotorClient(url)

    def get_collection(self, db_name: str, collection: str) -> AsyncIOMotorCollection:
        db = self.mongo_client[db_name]
        collection = db[collection]
        return collection

    async def get_all_documents(self, db_name: str, collection: str):
        collection = self.get_collection(db_name, collection)
        cursor = collection.find()
        mongo_documents = []
        for doc in await cursor.to_list(100):
            mongo_documents.append(doc)
        return mongo_documents

    async def insert_all(self, db_name: str, collection: str, documents: list[dict]):
        collection = self.get_collection(db_name, collection)
        await collection.insert_many(documents)
        logging.info(" Added documents to mongodb")
