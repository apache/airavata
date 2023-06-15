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

from .pikaproducer import PikaProducer

from threading import Thread, Lock, Event

import copy

# TODO: Refactor into subclass of Thread

class DataCollector(Thread):
    """Collects data by running user-specified routines.

    Inherits from: threading.Thread

    Instance variables:
    name -- the name of the collector
    limit -- the maximum number of maintained data points
    interval -- the interval (in seconds) at which data collection is performed

    Public methods:
    activate -- start collecting data
    add_routing_key -- add a new streaming endpoint
    deactivate -- stop further data collection
    remove_routing_key -- remove a streaming endpoint
    run -- collect data if active
    """
    def __init__(self, name, callback, rabbitmq_url, exchange, exchange_type="direct", limit=250, interval=10,
                 postprocessor=None, callback_args=[], postprocessor_args=[]):
        """
        Arguments:
        name -- the name of the collector
        callback -- the data collection function to run

        Keyword arguments:
        limit -- the maximum number of maintained data points (default 250)
        interval -- the time interval in seconds at which to collect data
                    (default: 10)
        postprocessor -- a function to run on the return value of callback
                         (default None)
        callback_args -- the list of arguments to pass to the callback
                         (default [])
        postprocessor_args -- the list of arguments to pass to the
                              postprocessor (default [])
        """
        super(DataCollector, self).__init__()
        self.name = name if name else "Unknown Resource"
        self.limit = limit
        self.interval = interval
        self._callback = callback
        self._callback_args = callback_args
        self._postprocessor = postprocessor
        self._postprocessor_args = postprocessor_args
        self._data = []
        self._data_lock = Lock()
        self._active = False
        self._producer = PikaProducer(rabbitmq_url, exchange, exchange_type=exchange_type, routing_keys=[])

    def activate(self):
        """
        Start collecting data.
        """
        self._active = True

    def add_routing_key(self, key):
        """
        Add a new producer endpoint.
        """
        self._producer.add_routing_key(key)


    def deactivate(self):
        """
        Stop collecting data.
        """
        self._active = False

    def remove_routing_key(self, key):
        self._producer.remove_routing_key(key)
        if len(self._producer.endpoints) == 0:
            self._producer.shutdown()

    def run(self):
        """
        Run the callback and postprocessing subroutines and record result.

        Catches generic exceptions because the function being run is not
        known beforehand.
        """
        self._collection_event = Event()
        while self._active and not self._collection_event.wait(timeout=self.interval):
            try:
                result = self._callback(*self._callback_args)
                result = self._postprocessor(result, *self._postprocessor_args) if self._postprocessor else result
                #print("Found the value ", result, " in ", self.name)
                self._data.append(result)
                if len(self._data) > self.limit:
                    self._data.pop(0)
                self._producer(copy.copy(self._data))

            except Exception as e:
                print("[ERROR] %s" % (e))

    def stop(self):
        for key in self.producer.routing_keys:
            self.remove_routing_key(key)
