import asyncio
import json

import uvicorn
from fastapi import FastAPI, WebSocket

app = FastAPI()


class PayLoad(object):
    def __init__(self, option_id: str, und_id: str, spot: float):
        self.option_id = option_id
        self.und_id = und_id
        self.spot = spot


@app.websocket("/quant")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    print("accepted connection")
    while True:
        data = await websocket.receive_text()
        print("accepted request")
        asyncio.create_task(process_request(data, websocket))
        print("completed request")


async def process_request(data: str, websocket):
    option_id = ""
    try:
        payload_dict = json.loads(data)
        option_id = payload_dict["option_id"]
        spot = payload_dict["spot"]

        # processing requests - calculate price
        await  asyncio.sleep(1)

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
    print("completed request {}", option_id)


if __name__ == '__main__':
    uvicorn.run(app)
