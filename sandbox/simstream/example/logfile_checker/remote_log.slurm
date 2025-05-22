#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

#SBATCH -J remote_logger     # Job name
#SBATCH -o remote_logger.o%j # Name of stdout output file(%j expands to jobId) 
#SBATCH -e remote_logger.o%j # Name of stderr output file(%j expands to jobId)
#SBATCH -p development         # large queue for jobs > 256 nodes
#SBATCH -t 00:10:00      # Run time (hh:mm:ss) - 1.5 hours
#SBATCH -n 1             # Nodes to use

module use "/home1/03947/tg832463/modulefiles"
module load openmm

touch test.txt

python log_streamer.py test.txt &

while true; do
    bash generate_logs.sh
    sleep 5
done

