#!/usr/bin/env bash

thrift --gen java sharing_models.thrift
cd gen-java
rm -r ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/models/*
cp -r org/apache/airavata/sharing/registry/models/ ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/models/

cd ..
thrift --gen java sharing_cpi.thrift
cd gen-java
rm -r ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/service/cpi/*
cp -r org/apache/airavata/sharing/registry/service/cpi/ ../../sharing-registry-stubs/src/main/java/org/apache/airavata/sharing/registry/service/cpi/

cd ..

rm -r gen-java

thrift --gen html sharing_models.thrift
thrift --gen html sharing_cpi.thrift

rm -r ../api-docs
mv gen-html ../api-docs
