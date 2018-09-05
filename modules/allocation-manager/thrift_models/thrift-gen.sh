#!/usr/bin/env bash
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

thrift --gen java:generated_annotations=undated allocation_manager_models.thrift
cd gen-java
rm -r ../../airavata-allocation-manager/airavata-allocation-manager-stubs/src/main/java/org/apache/airavata/allocation/manager/models/*
cp -r org/apache/airavata/allocation/manager/models/ ../../airavata-allocation-manager/airavata-allocation-manager-stubs/src/main/java/org/apache/airavata/allocation/manager/models/

cd ..
thrift --gen java:generated_annotations=undated allocation_manager_cpi.thrift
cd gen-java
rm -r ../../airavata-allocation-manager/airavata-allocation-manager-stubs/src/main/java/org/apache/airavata/allocation/manager/service/cpi/*
cp -r org/apache/airavata/allocation/manager/service/cpi/ ../../airavata-allocation-manager/airavata-allocation-manager-stubs/src/main/java/org/apache/airavata/allocation/manager/service/cpi/

cd ..

rm -r gen-java

thrift --gen html allocation_manager_models.thrift
thrift --gen html allocation_manager_cpi.thrift

rm -r ../allocation-manager-docs/api-docs
mv gen-html ../allocation-manager-docs/api-docs