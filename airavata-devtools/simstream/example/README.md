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

# SimStream Examples

This directory contains several examples showcasing the functionality of SimStream. To run them, download and install Python 2.7/3.5, install SimStream using setup.py, and modify the settings.json file to match your RabbitMQ server settings.

## The Examples

* mem_streamer: Stream max RSS memory consumed by a basic SimStream utility
* logfile_checker: Collect, filter, and stream tagged log file entries
* openmm_example: Run a molecular dynamics simulation and return log information and system state measured by root mean squared deviation