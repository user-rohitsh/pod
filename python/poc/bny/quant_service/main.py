import asyncio
import json
import logging

import uvicorn
from fastapi import FastAPI, WebSocket
from websockets.exceptions import ConnectionClosedError

app = FastAPI()


class PayLoad(object):
    def __init__(self, option_id: str, und_id: str, spot: float):
        self.option_id = option_id
        self.und_id = und_id
        self.spot = spot


@app.websocket("/quant")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    logging.info("accepted connection")
    while True:
        try:
            data = await websocket.receive_text()
            asyncio.create_task(process_request(data, websocket))
        except (ConnectionClosedError, ConnectionAbortedError, ConnectionResetError) as ex:
            logging.error("Connection closed")
            break


async def process_request(data: str, websocket):
    option_id = ""
    spot = ""
    try:
        payload_dict = json.loads(data)
        option_id = payload_dict["option_id"]
        spot = payload_dict["spot"]

        logging.info("Accepted request for option_id = {} spot = {}".format(option_id, spot))

        # processing requests - calculate price
        await  asyncio.sleep(5)

        reply = {
            "option_id": option_id,
            "option_value": spot,
            "spot": spot,
            "beta": "1.0"
        }
        reply_json = json.dumps(reply)
    except Exception as ex:
        reply_json = "{}"

    await websocket.send_text(reply_json)
    logging.info("Completed request for option_id = {} spot = {}".format(option_id, spot))


if __name__ == '__main__':
    logging.basicConfig(filename="{}.log".format("quant_service"),
                        filemode='w',
                        format='%(asctime)s,%(thread)d %(message)s',
                        datefmt='%H:%M:%S',
                        level=logging.INFO)
    uvicorn.run(app)
