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

# Research Service

The Research Service provides the research catalog API (notebooks, datasets, models). It is embedded in the unified Airavata server and is not run as a standalone application.

## Development

The research service is started automatically as part of the unified server (`airavata-server`). Use `tilt up` from the repo root to start the full development stack, which includes the research service on port 9090 alongside all other services.

## Profiles

The service supports Spring Boot profiles:
- **default (production)**: Security filter enforces authentication
- **dev**: Security filter is bypassed for easier local testing

When running via the unified server, the active profile is controlled by the server's `application.yml` configuration.
