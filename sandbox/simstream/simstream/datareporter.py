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
Utilties for collecting system data.

Author: Jeff Kinnison (jkinniso@nd.edu)
"""

# TODO: Refactor to iterate over producers, not collectors. Collectors should
#       execute concurrently.
# TODO: Add method to deactivate reporter

from threading import Thread, Event

from .datacollector import DataCollector


class CollectorExistsException(Exception):
    """Thrown when attempting to add a collector with a conflicting name."""
    pass


class CollectorDoesNotExistException(Exception):
    """Thrown when attempting to access a collector that does not exist."""
    pass


class DataReporter(object):
    """Manages collecting specified data.

    Subclass of threading.Thread that modifies Thread.join() and Thread.run()

    Instance variables:
    collectors -- a dict of DataCollectors that are run at interval

    Public methods:
    add_collector -- add a new DataCollector to the list
    run -- start the data collection loop
    join -- end data collection and return control to main thread
    start_collecting -- begin data collection for all collectors
    start_collector -- begin data collection for a specific collector
    stop_collecting -- stop all data collection
    stop_collector -- stop a running DataCollector
    """

    def __init__(self, collectors={}):
        super(DataReporter, self).__init__()
        self.collectors = {}
        for key, value in collectors:
            self.add_collector(
                key,
                value.limit,
                value.callback,
                value.url,
                value.exchange,
                value.postprocessor,
                value.callback_args,
                value.postprocessor_args
            )

    def add_collector(self, name, callback, rabbitmq_url, exchange, limit=250, interval=10, postprocessor=None,
                      exchange_type="direct", callback_args=[], postprocessor_args=[]):
        """Add a new collector.

        Arguments:
        name -- name of the new DataCollector
        callback -- the data collection callback to run

        Keyword arguments:
        limit -- the number of data points to store (default 100)
        postprocessor -- a postprocessing function to run on each data point
                         (default None)
        callback_args -- a list of arguments to pass to the callback
                         (default [])
        postprocessor_args -- a list of arguments to pass to the postprocessor
                              (default [])

        Raises:
        CollectorExistsException if a collector named name already exists
        """
        if name in self.collectors:
            raise CollectorExistsException

        self.collectors[name] = DataCollector(
            name,
            callback,
            rabbitmq_url,
            exchange,
            limit=limit,
            interval=interval,
            postprocessor=postprocessor,
            exchange_type=exchange_type,
            callback_args=callback_args,
            postprocessor_args=postprocessor_args
        )

    def start_collecting(self):
        """
        Start data collection for all associated collectors.
        """
        for collector in self.collectors:
            self.start_collector(collector)

    def start_collector(self, name):
        """
        Activate the specified collector.

        Arguments:
        name -- the name of the collector to start

        Raises:
        RuntimeError if the collector has already been started.
        """
        try:
            self.collectors[name].activate()
            self.collectors[name].start()
        except RuntimeError as e:
            print("Error starting collector ", name)
            print(e)

    def stop_collecting(self):
        """
        Stop all collectors.
        """
        for collector in self.collectors:
            self.stop_collector(collector)

    def stop_collector(self, name):
        """Deactivate the specified collector.

        Arguments:
        name -- the name of the collector to stop

        Raises:
        CollectorDoesNotExistException if no collector named name exists
        """
        if name not in self.collectors:
            raise CollectorDoesNotExistException

        try:
            self.collectors[name].deactivate()
            self.collectors[name].join()
        except RuntimeError as e: # Catch deadlock
            print(e)


    def start_streaming(self, collector_name, routing_key):
        """
        Begin streaming data from a collector to a particular recipient.

        Arguments:
        routing_key -- the routing key to reach the intended recipient
        """
        if collector_name not in self.collectors: # Make sure collector exists
            raise CollectorDoesNotExistException
        self.collectors[collector_name].add_routing_key(routing_key)

    def stop_streaming(self, collector_name, routing_key):
        """
        Stop a particular stream.

        Arguments:
        collector_name -- the collector associated with the producer to stop
        routing_key -- the routing key to reach the intended recipient

        Raises:
        ProducerDoesNotExistException if no producer named name exists
        ValueError if the producer is removed by another call to this method
                   after the for loop begins
        """
        pass
