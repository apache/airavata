#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

"""
Utilties for sending data.

Author: Jeff Kinnison (jkinniso@nd.edu)
"""

import json
import pika


class PikaProducer(object):
    """
    Utility for sending job data to a set of endpoints.
    """

    def __init__(self, rabbitmq_url, exchange, exchange_type="direct", routing_keys=[]):
        """
        Instantiate a new PikaProducer.

        Arguments:
        rabbitmq_url -- the url of the RabbitMQ server to send to
        exchange -- the name of the exchange to send to

        Keyword Arguments:
        exchange_type -- one of one of 'direct', 'topic', 'fanout', 'headers'
                         (default 'direct')
        routing_key -- the routing keys to the endpoints for this producer
                       (default [])
        """
        self._url = rabbitmq_url
        self._exchange = exchange
        self._exchange_type = exchange_type
        self._routing_keys = routing_keys

        self._connection = None # RabbitMQ connection object
        self._channel = None    # RabbitMQ channel object

        import random
        self._name = random.randint(0,100)

    def __call__(self, data):
        """
        Publish data to the RabbitMQ server.

        Arguments:
        data -- JSON serializable data to send
        """
        if self._connection is None: # Start the connection if it is inactive
            self.start()
        else: # Serialize and send the data
            message = self.pack_data(data)
            self.send_data(message)

    def add_routing_key(self, key):
        """
        Add a new endpoint that will receive this data.

        Arguments:
        key -- the routing key for the new endpoint
        """
        if key not in self._routing_keys:
            #print("Adding key %s to %s" % (key, self._name))
            self._routing_keys.append(key)
            #print(self._routing_keys)

    def remove_routing_key(self, key):
        """
        Stop sending data to an existing endpoint.

        Arguments:
        key -- the routing key for the existing endpoint
        """
        try:
            self._routing_keys.remove(key)
        except ValueError:
            pass

    def pack_data(self, data):
        """
        JSON-serialize the data for transport.

        Arguments:
        data -- JSON-serializable data
        """
        try: # Generate a JSON string from the data
            msg = json.dumps(data)
        except TypeError as e: # Generate and return an error if serialization fails
            msg = json.dumps({"err": str(e)})
        finally:
            return msg

    def send_data(self, data):
        """
        Send the data to all active endpoints.

        Arguments:
        data -- the message to send
        """
        if self._channel is not None: # Make sure the connection is active
            for key in self._routing_keys: # Send to all endpoints
                #print(self._exchange, key, self._name)
                self._channel.basic_publish(exchange = self._exchange,
                                            routing_key=key,
                                            body=data)

    def start(self):
        """
        Open a connection if one does not exist.
        """
        print("Starting new connection")
        if self._connection is None:
            print("Creating connection object")
            self._connection = pika.BlockingConnection(pika.URLParameters(self._url))
            self._channel = self._connection.channel()
            self._channel.exchange_declare(exchange=self._exchange,
                                           type=self._exchange_type)

    def shutdown(self):
        """
        Close an existing connection.
        """
        if self._channel is not None:
            self._channel.close()

    def _on_connection_open(self, unused_connection):
        """
        Create a new channel if the connection opens successful.

        Arguments:
        unused_connection -- a reference to self._connection
        """
        print("Connection is open")
        self._connection.channel(on_open_callback=self._on_channel_open)

    def _on_connection_close(self, connection, code, text):
        """
        Actions to take when the connection is closed for any reason.

        Arguments:
        connection -- the connection that was closed (same as self._connection)
        code -- response code from the RabbitMQ server
        text -- response body from the RabbitMQ server
        """
        print("Connection is closed")
        self._channel = None
        self._connection = None

    def _on_channel_open(self, channel):
        """
        Actions to take when the channel opens.

        Arguments:
        channel -- the newly opened channel
        """
        print("Channel is open")
        self._channel = channel
        self._channel.add_on_close_callback(self._on_channel_close)
        self._declare_exchange()

    def _on_channel_close(self, channel, code, text):
        """
        Actions to take when the channel closes for any reason.

        Arguments:
        channel -- the channel that was closed (same as self._channel)
        code -- response code from the RabbitMQ server
        text -- response body from the RabbitMQ server
        """
        print("Channel is closed")
        self._connection.close()

    def _declare_exchange(self):
        """
        Set up the exchange to publish to even if it already exists.
        """
        print("Exchange is declared")
        self._channel.exchange_declare(exchange=self._exchange,
                                       type=self.exchange_type)

if __name__ == "__main__":
    import time

    config = {
        "url": "amqp://guest:guest@localhost:5672",
        "exchange": "simstream",
        "routing_key": "test_consumer",
        "exchange_type": "topic"
    }

    producer = PikaProducer(config["url"],
                            config["exchange"],
                            exchange_type=config["exchange_type"],
                            routing_keys=[config["routing_key"]])
    producer.start()

    while True:
        try:
            time.sleep(5)
            data = str(time.time()) + ": Hello SimStream"
            producer.send_data(data)
        except KeyboardInterrupt:
            producer.shutdown()
