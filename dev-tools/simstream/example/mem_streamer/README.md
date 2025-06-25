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

# SimStream Example: Memory Usage Streamer

This example collects data on the memory used by the Publisher and sends that data to the Consumer.

## Instructions

### Start the Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_consumer.py`

### Starting the Consumer
1. Open a new terminal
2. `cd path/to/simstream/examples/mem_streamer`
3. `python memory_consumer.py

The Consumer should receive the memory used by the Publisher (KB) and the time that the data was collected (s since UNIX epoch) at a 2-second interval.
