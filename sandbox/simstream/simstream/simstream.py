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

import pika

from .pikaasyncconsumer import PikaAsyncConsumer
from .datacollector import DataCollector
from .datareporter import DataReporter
from .eventhandler import EventHandler
from .eventmonitor import EventMonitor


class ReporterExistsException(Exception):
    """Thrown when attempting to add a DataReporter with a conflicting name"""
    pass


class SimStream(object):
    """
    Manager for routing messages to their correct reporter.
    """

    DEFAULT_CONFIG_PATH="simstream.cnf"


    class MessageParser(object):
        """
        Internal message parsing facilities.
        """

        def __init__(self):
            self.parsed = None

        def __call__(self, message):
            pass


    def __init__(self, reporters={}, config={}):
        self.reporters = reporters
        self.consumer = None
        self.config = config

    def add_data_reporter(self, reporter):
        """
        Add a new DataReporter object.

        Arguments:
        reporter -- the DataReporter to add
        """
        if reporter.name in self.reporters:
            raise ReporterExistsException
        self.reporters[reporter.name] = reporter

    def parse_config(self):
        """
        Read the config file and set up the specified, data collection and
        event handling resources.
        """
        # TODO: Read in config
        # TODO: Set up configuration dict
        pass

    def route_message(self, message):
        """
        Send a message to the correct reporter.
        """
        # TODO: Create new MessageParser
        # TODO: Run message through MessageParser
        # TODO: Route message to the correct DataReporter/EventMonitor
        parser = MessageParser()
        parser(message)
        if parser.reporter_name in self.reporters:
            self.reporters[parser.reporter_name].start_streaming(
                    parser.collector_name,
                    parser.routing_key
                )

    def start_collecting(self):
        """
        Begin collecting data and monitoring for events.
        """
        for reporter in self.reporters:
            self.reporters[reporter].start_collecting()

    def setup(self):
        """
        Set up the SimStream instance: create DataCollectors, create
        EventMonitors, configure AMQP consumer.
        """
        self.parse_config()
        #self.setup_consumer()
        self.setup_data_collection()
        self.setup_event_monitoring()

    def setup_data_collection(self):
        """
        Set up all DataReporters and DataCollectors.
        """
        # TODO: Create and configure all DataReporters
        # TODO: Create and configure all DataCollectors
        # TODO: Assign each DataCollector to the correct DataReporter
        if "reporters" in self.config:
            for reporter in self.config.reporters:
                pass
            for collector in self.config.collectors:
                pass

    def setup_event_monitoring(self):
        #TODO: Create and configure all EventMonitors
        #TODO: Create and configure all EventHandlers
        #TODO: Assign each EventHandler to the correct EventMonitor
        #TODO: Assign each EventMonitor to the correct DataCollector
        pass

    def setup_consumer(self):
        """
        Set up and configure the consumer.
        """
        if len(self.config) > 0 and self.consumer is None:
            if "message_handler" in self.config:
                message_handler = self.config["message_handler"]
            else:
                message_handler = self.route_message
            self.consumer = PikaAsyncConsumer(self.config["url"],
                                              self.config["exchange"],
                                              self.config["queue"],
                                              message_handler,
                                              exchange_type=self.config["exchange_type"],
                                              routing_key=self.config["routing_key"]
                                             )

    def start(self):
        """
        Configure and start SimStream.
        """
        if self.consumer is None:
            self.setup()
        self.start_collecting()
        #self.consumer.start()

    def stop(self):
        """
        Stop all data collection, event monitoring, and message consumption.
        """
        self.consumer.stop()
        self.stop_collecting()


if __name__ == "__main__":
    def print_message(message):
        with open("test.out", "w") as f:
            print(message)

    print(SimStream.DEFAULT_CONFIG_PATH)

    config = {
        "url": "amqp://guest:guest@localhost:5672",
        "exchange": "simstream",
        "queue": "simstream_test",
        "message_handler": print_message,
        "routing_key": "test_consumer",
        "exchange_type": "topic"
    }

    streamer = SimStream(config=config)

    try:
        streamer.start()
    except KeyboardInterrupt:
        streamer.stop()
