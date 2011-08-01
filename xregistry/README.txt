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

The XRegistry service is compiled using Maven2.
mvn clean install
cd target/dist-bin
./xregistry.sh xregistry.properties

To configure MySQL server for production purpose. 

While building service
uncomment mysql dependency in pom.xml
Change databaseDriver and databaseUrl in pom.xml and rebuild by mvn clean install

After build
Download the mysql driver "mysql-connector-java" and copy it to target/dist-bin/lib
Update xregistry.properties for jdbcDriver and databaseUrl.
cd target/dist-bin
./xregistry.sh xregistry.properties

