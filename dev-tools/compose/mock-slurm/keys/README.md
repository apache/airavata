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

# ⚠️ Development-Only Test Fixture Keys

The SSH key pair in this directory is a **test fixture** checked into the
repository intentionally. It exists solely to authenticate against the
local `mock-slurm` Docker container used during development.

**These keys grant no access to any production system, staging environment,
or external service.** They authenticate only to a container you run
locally via `docker compose up`.

Do **not** use these keys for any purpose other than local development
with the mock Slurm container.
