<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# SimStream Example: Logfile Streaming

This example filters log file entries by starting tag and sends them to a remote listener. The listener prints the logs it receives to terminal.

## Instructions

### Start the Publisher
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_streamer.py`

### Start the Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_consumer.py`

### Write Some Logs
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `chmod 700 generate_logs.sh`
4. `./generate_logs.sh`

This will write logs to `test.txt`. The Publisher will continuously check for new logs, filter based on the [STATUS] and [ERROR] tags, and send the filtered results to the RabbitMQ server. The Consumer will receive the filtered log entries and print them to the terminal.
