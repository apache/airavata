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

import json
from simstream import PikaAsyncConsumer

settings = {}

with open("../settings.json", 'r') as f:
    settings = json.load(f)
settings["routing_key"] = "openmm.log"

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

consumer = PikaAsyncConsumer(settings["url"],
                             settings["exchange"],
                             "openmm.log", # settings["queue"],
                             message_handler=print_log_line,
                             routing_key=settings["routing_key"],
                             exchange_type=settings["exchange_type"])

if __name__ == "__main__":
    try:
        consumer.start()
    except KeyboardInterrupt:
        consumer.stop()
