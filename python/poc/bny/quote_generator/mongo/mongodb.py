from pymongo import MongoClient


class Mongo(object):
    def __init__(self, url):
        self.mongo_client: MongoClient = MongoClient(url)

    def read_all_documents(self, db_name: str, collection: str):
        db = self.mongo_client[db_name]
        option_data = db[collection]
        return option_data.find()
