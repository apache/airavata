====

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
====

This tool contains clients to airavata/tools/gsissh libraries.  To compile, you must first run "mvn clean install" in airavata/tools/gsissh. Then run "mvn clean install" in airavata/tools/gsissh-cli-tools.


To run the SSHApiClientWithMyProxyAuth tool, first add all the jars in target/lib to a variable that you'll use when setting your classpath:

export CP=`echo ./target/lib/*.jar | tr ' ' ':'`

Then use the following command line:
java -classpath ./target/gsissh-cli-tools-0.14-SNAPSHOT.jar:$CP -Dmyproxy.server=myproxy.teragrid.org -Dmyproxy.username=<your.username> -Dmyproxy.password=<your.password> -Dmyproxy.cert.location=<path-to>/airavata/tools/gsissh-cli-tools/target/classes/certificates/ -Dremote.host=trestles.sdsc.xsede.org -Dremote.host.port=22 -Dremote.cmd=/bin/ls org.apache.airavata.gfac.ssh.cli.SSHApiClientWithMyProxyAuth
