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

# Research Service Application

This Spring Boot application supports different profiles for running in production vs development mode. In production mode, a security filter enforces authentication. In development mode, the security filter is bypassed for easier local testing.

## Running in Development Mode

### Using Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Using IntelliJ IDEA

1. Go to Run > Edit Configurations.
2. Select your Spring Boot run configuration
3. In the Program arguments field, add:

```bash
--spring.profiles.active=dev
```
