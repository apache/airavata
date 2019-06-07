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

SHARING_CPI="../../../thrift-interface-descriptions/component-cpis/sharing_cpi.thrift "
SHARING_MODELS="../../../thrift-interface-descriptions/data-models/sharing-models/sharing_models.thrift"
# thrift --gen java:generated_annotations=undated sharing_models.thrift
# cd gen-java
# rm -r ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/models/*
# cp -r org/apache/airavata/sharing/registry/models/ ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/models/

# cd ..
# thrift --gen java:generated_annotations=undated sharing_cpi.thrift
# cd gen-java
# rm -r ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/service/cpi/*
# cp -r org/apache/airavata/sharing/registry/service/cpi/ ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/service/cpi/

# cd ..

# rm -r gen-java

thrift --gen html $SHARING_MODELS
thrift --gen html $SHARING_CPI

rm -r ../sharing-service-docs/api-docs
mv gen-html ../sharing-service-docs/api-docs
