import json
from simstream import PikaAsyncConsumer

#settings = {
#    "url": "amqp://guest:guest@localhost:5672",
#    "exchange": "simstream",
#    "queue": "test",
#    "routing_key": "logfile",
#    "exchange_type": "topic"
#}

settings = {}

with open("../settings.json", 'r') as f:
    settings = json.load(f)
    settings["routing_key"] = "memory"

def print_log_line(body):
    try:
        lines = json.loads(body.decode())
        if lines is not None:
            for line in lines:
                print(line)
    except json.decoder.JSONDecodeError as e:
        print("[Error]: Could not decode %s" % (body))
    except UnicodeError as e:
        print("[Error]: Could not decode from bytes to string: %s" % (e.reason))


consumer = PikaAsyncConsumer(
                            settings["url"],
                            settings["exchange"],
                            settings["queue"],
                            print_log_line,
                            exchange_type=settings["exchange_type"],
                            routing_key=settings["routing_key"]
                            )

if __name__ == "__main__":
    try:
        consumer.start()
    except KeyboardInterrupt:
        consumer.stop()
