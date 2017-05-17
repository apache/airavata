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
Streaming utility for system and simulation data.

author: Jeff Kinnison (jkinniso@nd.edu)
"""

import json
import pika

class PikaAsyncConsumer(object):
    """
    The primary entry point for routing incoming messages to the proper handler.
    """

    def __init__(self, rabbitmq_url, exchange_name, queue_name, message_handler,
                 exchange_type="direct", routing_key="#"):
        """
        Create a new instance of Streamer.

        Arguments:
        rabbitmq_url -- URL to RabbitMQ server
        exchange_name -- name of RabbitMQ exchange to join
        queue_name -- name of RabbitMQ queue to join

        Keyword Arguments:
        exchange_type -- one of 'direct', 'topic', 'fanout', 'headers'
                         (default 'direct')
        routing_keys -- the routing key that this consumer listens for
                        (default '#', receives all messages)
        """
        self._connection = None
        self._channel = None
        self._shut_down = False
        self._consumer_tag = None
        self._url = rabbitmq_url
        self._message_handler = message_handler

        # The following are necessary to guarantee that both the RabbitMQ
        # server and Streamer know where to look for messages. These names will
        # be decided before dispatch and should be recorded in a config file or
        # else on a per-job basis.
        self._exchange = exchange_name
        self._exchange_type = exchange_type
        self._queue = queue_name
        self._routing_key = routing_key

    def connect(self):
        """
        Create an asynchronous connection to the RabbitMQ server at URL.
        """
        return pika.SelectConnection(pika.URLParameters(self._url),
                                     on_open_callback=self.on_connection_open,
                                     on_close_callback=self.on_connection_close,
                                     stop_ioloop_on_close=False)

    def on_connection_open(self, unused_connection):
        """
        Actions to perform when the connection opens. This may not happen
        immediately, so defer action to this callback.

        Arguments:
        unused_connection -- the created connection (by this point already
                             available as self._connection)
        """
        self._connection.channel(on_open_callback=self.on_channel_open)

    def on_connection_close(self, connection, code, text):
        """
        Actions to perform when the connection is unexpectedly closed by the
        RabbitMQ server.

        Arguments:
        connection -- the connection that was closed (same as self._connection)
        code -- response code from the RabbitMQ server
        text -- response body from the RabbitMQ server
        """
        self._channel = None
        if self._shut_down:
            self._connection.ioloop.stop()
        else:
            self._connection.add_timeout(5, self.reconnect)

    def reconnect(self):
        """
        Attempt to reestablish a connection with the RabbitMQ server.
        """
        self._connection.ioloop.stop() # Stop the ioloop to completely close

        if not self._shut_down: # Connect and restart the ioloop
            self._connection = self.connect()
            self._connection.ioloop.start()

    def on_channel_open(self, channel):
        """
        Store the opened channel for future use and set up the exchange and
        queue to be used.

        Arguments:
        channel -- the Channel instance opened by the Channel.Open RPC
        """
        self._channel = channel
        self._channel.add_on_close_callback(self.on_channel_close)
        self.declare_exchange()


    def on_channel_close(self, channel, code, text):
        """
        Actions to perform when the channel is unexpectedly closed by the
        RabbitMQ server.

        Arguments:
        connection -- the connection that was closed (same as self._connection)
        code -- response code from the RabbitMQ server
        text -- response body from the RabbitMQ server
        """
        self._connection.close()

    def declare_exchange(self):
        """
        Set up the exchange that will route messages to this consumer. Each
        RabbitMQ exchange is uniquely identified by its name, so it does not
        matter if the exchange has already been declared.
        """
        self._channel.exchange_declare(self.declare_exchange_success,
                                        self._exchange,
                                        self._exchange_type)

    def declare_exchange_success(self, unused_connection):
        """
        Actions to perform on successful exchange declaration.
        """
        self.declare_queue()

    def declare_queue(self):
        """
        Set up the queue that will route messages to this consumer. Each
        RabbitMQ queue can be defined with routing keys to use only one
        queue for multiple jobs.
        """
        self._channel.queue_declare(self.declare_queue_success,
                                    self._queue)

    def declare_queue_success(self, method_frame):
        """
        Actions to perform on successful queue declaration.
        """
        self._channel.queue_bind(self.munch,
                                 self._queue,
                                 self._exchange,
                                 self._routing_key
                                )

    def munch(self, unused):
        """
        Begin consuming messages from the Airavata API server.
        """
        self._channel.add_on_cancel_callback(self.cancel_channel)
        self._consumer_tag = self._channel.basic_consume(self._process_message)

    def cancel_channel(self, method_frame):
        if self._channel is not None:
            self._channel._close()

    def _process_message(self, ch, method, properties, body):
        """
        Receive and verify a message, then pass it to the router.

        Arguments:
        ch -- the channel that routed the message
        method -- delivery information
        properties -- message properties
        body -- the message
        """
        print("Received Message: %s" % body)
        self._message_handler(body)
        #self._channel.basic_ack(delivery_tag=method.delivery_tag)

    def stop_consuming(self):
        """
        Stop the consumer if active.
        """
        if self._channel:
            self._channel.basic_cancel(self.close_channel, self._consumer_tag)

    def close_channel(self):
        """
        Close the channel to shut down the consumer and connection.
        """
        self._channel.close()

    def start(self):
        """
        Start a connection with the RabbitMQ server.
        """
        self._connection = self.connect()
        self._connection.ioloop.start()

    def stop(self):
        """
        Stop an active connection with the RabbitMQ server.
        """
        self._closing = True
        self.stop_consuming()
