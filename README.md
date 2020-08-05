# Apache Airavata

[![Build Status](https://travis-ci.org/apache/airavata.svg?branch=master)](https://travis-ci.org/apache/airavata)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.airavata/airavata/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.airavata%22)

## About

Apache Airavata is a software framework for executing and managing computational
jobs on distributed computing resources including local clusters,
supercomputers, national grids, academic and commercial clouds. Airavata builds
on general concepts of service oriented computing, distributed messaging, and
workflow composition and orchestration. Airavata bundles a server package with
an API, client software development Kits and a general purpose reference UI
implementation -
[Apache Airavata Django Portal](https://github.com/apache/airavata-django-portal).

Learn more about Airavata at
[https://airavata.apache.org](https://airavata.apache.org).

## Building Apache Airavata

### Prerequisites

- Sources compilation requires Java SDK 8.
- The project is built with Apache Maven 3+.
- Set or export JAVA_HOME to point to JDK. For example in Ubuntu:
  `export JAVA_HOME=/usr/lib/jvm/java-6-openjdk`
- Git

### Build the distribution

    git clone https://github.com/apache/airavata.git
    cd airavata
    mvn clean install

To build without running tests, use `mvn clean install -Dmaven.test.skip=true`.
The compressed binary distribution is created at
PROJECT_DIR/modules/distribution/target.

## Getting Started

The easiest way to get started with running Airavata locally and setting up a
development environment is to follow the instructions in the
[ide-integration README](./modules/ide-integration/README.md). Those
instructions will guide you on setting up a development environment with
IntelliJ IDEA.

## Contact

For additional information about Apache Airavata, please contact the user or dev
mailing lists: https://airavata.apache.org/mailing-list.html

## Contributing

Want to help contribute to the development of Apache Airavata? Check out our
[contributing documentation](http://airavata.apache.org/get-involved.html).

## Links

- [Documentation](https://docs.airavata.org/en/master/)
- Developer [wiki](https://cwiki.apache.org/confluence/display/AIRAVATA)
- [Issue Tracker](https://issues.apache.org/jira/projects/AIRAVATA)

## License

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

Please see the [LICENSE](LICENSE) file included in the root directory of the
source tree for extended license details.
