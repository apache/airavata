import json
from simstream import PikaAsyncConsumer

def recv_log(body):
    try:
        logs = json.loads(body.decode())
        for log in logs:
            print(log)