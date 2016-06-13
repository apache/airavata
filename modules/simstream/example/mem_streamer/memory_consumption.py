import tornado.ioloop
import tornado.web
import tornado.websocket

import json

from simstream import PikaAsyncConsumer, PikaProducer

#settings = {
#    "url": "amqp://localhost:5672",
#    "exchange": "simstream",
#    "queue": "remote_node",
#    "routing_key": "test",
#    "exchange_type": "topic"
#}

settings = {}
 
with open("../settings.json", 'r') as f:
    settings = json.load(f)
    settings["routing_key"] = "memory"


def print_result(body):
    try:
        data = json.loads(body.decode())
        print("%s: %s" % (data["x"], data["y"]))
    except json.decoder.JSONDecodeError as e:
        print("[ERROR] Could not decode JSON %s: %s", (body, e))
    except UnicodeError as e:
        print("[ERROR] Could not decode message %s: %s" % (body, e.reason))

consumer = PikaAsyncConsumer(settings['url'],
                             settings['exchange'],
                             settings['queue'],
                             print_result,
                             exchange_type=settings['exchange_type'],
                             routing_key=settings['routing_key'])

consumer.start()

# class PlotHandler(tornado.web.RequestHandler):

#     def get(self):
#         pass


# class StreamingHandler(tornado.websocket.WebSocketHandler):

#     def open(self):
#         self.consumer = PikaAsyncConsumer(settings.url,
#                                           settings.exchange,
#                                           settings.queue,
#                                           self.send_data,
#                                           routing_keys=settings.routing_key,
#                                           exchange_type=settings.exchange_type
#                                           )
#         self.producer = PikaProducer("",
#                                      remote_settings.url,
#                                      remote_settings.exchange,
#                                      remote_settings.queue,
#                                      remote_settings.routing_key)

#     def on_message(self, message):
#         if hasattr(self, producer) and producer is not None:
#             self.producer.send_data(message)

#     def on_close(self):
#         self.consumer.stop()
#         self.producer.shutdown()
#         self.consumer = None
#         self.producer = None

#     def send_data(self, ch, method, properties, body):
#         self.write_message(body)

# if __name__ == "__main__":
#     app = tornado.web.Application([
#             (r"/plot/(.*)", )
#             (r"/stream/(.*)", StreamingHandler)
#         ])
#     app.listen(8888)
#     tornado.ioloop.IOLoop.current().start()
