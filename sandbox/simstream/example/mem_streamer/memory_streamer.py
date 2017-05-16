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

import resource
import time
import json

from simstream import SimStream, DataReporter, DataCollector

#settings = {
#    "url": "amqp://localhost:5672",
#    "exchange": "simstream",
#    "queue": "remote_node",
#    "routing_key": "stream_sender",
#    "exchange_type": "topic"
#}

settings = {}

with open("../settings.json", 'r') as f:
    settings = json.load(f)
    settings["routing_key"] = "memory"

def mem_callback():
    return {'x': time.time() * 1000,
            'y': resource.getrusage(resource.RUSAGE_SELF).ru_maxrss}


def mem_postprocessor(rss):
    rss.y  = rss.y / 1000000
    return rss

mem_reporter = DataReporter()
mem_reporter.add_collector("rss",
                           mem_callback,
                           settings["url"],
                           settings["exchange"],
                           limit=100,
                           interval=2,
                           postprocessor=mem_postprocessor,
                           )

mem_reporter.start_streaming("rss", "test")

if __name__ == "__main__":
    resource_streamer = SimStream(reporters={"memory": mem_reporter},
                                  config=settings)
    resource_streamer.setup()
    resource_streamer.start()
