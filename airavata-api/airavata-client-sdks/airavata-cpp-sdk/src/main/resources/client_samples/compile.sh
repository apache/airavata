
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

g++ -I/home/ixxi-2013/Desktop/airavata-trunk/airavata/airavata-api/airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/ -L/usr/local/lib -w -Wno-write-strings   -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H  `pkg-config --cflags glib-2.0` createProject.cpp `pkg-config --libs glib-2.0` -lthrift -o createProject
g++ -I/home/ixxi-2013/Desktop/airavata-trunk/airavata/airavata-api/airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/ -L/usr/local/lib -w -Wno-write-strings   -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H  `pkg-config --cflags glib-2.0` createExperiment.cpp `pkg-config --libs glib-2.0` -lthrift -o createExperiment
g++ -I/home/ixxi-2013/Desktop/airavata-trunk/airavata/airavata-api/airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/ -L/usr/local/lib -w -Wno-write-strings   -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H  `pkg-config --cflags glib-2.0` launchExperiment.cpp `pkg-config --libs glib-2.0` -lthrift -o launchExperiment
g++ -I/home/ixxi-2013/Desktop/airavata-trunk/airavata/airavata-api/airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/ -L/usr/local/lib -w -Wno-write-strings   -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H  `pkg-config --cflags glib-2.0` getExperimentStatus.cpp `pkg-config --libs glib-2.0` -lthrift -o getExperimentStatus
g++ -I/home/ixxi-2013/Desktop/airavata-trunk/airavata/airavata-api/airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/ -L/usr/local/lib -w -Wno-write-strings   -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H  `pkg-config --cflags glib-2.0` getExperimentOutputs.cpp `pkg-config --libs glib-2.0` -lthrift -o getExperimentOutputs
