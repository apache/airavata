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

# Local identity provider (Dex)

An optional [Dex](https://dexidp.io) instance with two test users, so you can call the
API as ordinary users (not only the root account) without a CILogon client. The server
needs no changes: it validates Dex tokens through the same `CILOGON_*` introspection
settings it uses for CILogon. Development only.

| User | Password |
|---|---|
| `alice@example.com` | `password` |
| `bob@example.com` | `password` |

## Usage

Start Dex (it only runs with the `auth` profile):

```bash
docker compose -f dev-tools/compose/compose.yml --profile auth up -d postgres dex
```

Point the server at it:

```bash
CILOGON_ISSUER=http://127.0.0.1:5556/dex \
CILOGON_INTROSPECTION_URI=http://127.0.0.1:5556/dex/token/introspect \
CILOGON_USERINFO_URI=http://127.0.0.1:5556/dex/userinfo \
CILOGON_CLIENT_ID=airavata CILOGON_CLIENT_SECRET=airavata-dev-secret \
AIRAVATA_ROOT_ACCOUNT_TOKEN=dev-token \
./bin/airavata-server
```

Get an access token for a user:

```bash
ALICE=$(curl -s -u airavata:airavata-dev-secret http://127.0.0.1:5556/dex/token -d grant_type=password -d username=alice@example.com -d password=password -d scope=openid | jq -r .access_token)
```

A user's first request creates their account as `SUSPENDED`, as with CILogon. Activate
it with the root token:

```bash
ALICE_ID=$(curl -s localhost:9095/api/v1/users/me -H "Authorization: Bearer $ALICE" | jq -r .userId)
```

```bash
curl -s -X POST "localhost:9095/api/v1/users/$ALICE_ID/status" -H "Authorization: Bearer dev-token" -H "Content-Type: application/json" -d '{"status":"ACTIVE"}'
```

## Limitations

- **Dashboard sign-in does not work.** The server requests the CILogon-specific scope
  `org.cilogon.userinfo`, which Dex rejects with `invalid_scope`. Use API tokens.
- **User ids are opaque.** Dex sends no `username` claim, so the server uses its `sub`
  (for example `CgQxMDAxEgVsb2NhbA`) rather than a `cilogon:<id>` name.
- **Tokens do not survive a restart.** Dex keeps its state in memory, so get new
  tokens after restarting it. Accounts already created in Airavata are kept.
