#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import time

import requests


class DeviceFlowAuthenticator:

    idp_url: str
    realm: str
    client_id: str
    device_code: str | None
    interval: int
    access_token: str | None
    refresh_token: str | None

    @property
    def logged_in(self) -> bool:
        return self.access_token is not None

    def __init__(
        self,
        idp_url: str,
        realm: str,
        client_id: str,
    ):
        self.idp_url = idp_url
        self.realm = realm
        self.client_id = client_id

        if not self.client_id or not self.realm or not self.idp_url:
            raise ValueError(
                "Missing required environment variables for client ID, realm, or auth server URL")

        self.device_code = None
        self.interval = -1
        self.access_token = None

    def login(self, interactive: bool = True):
        # Step 0: Check if we have a saved token
        if self.__load_saved_token__():
            print("Using saved token")
            return

        # Step 1: Request device and user code
        auth_device_url = f"{self.idp_url}/realms/{self.realm}/protocol/openid-connect/auth/device"
        response = requests.post(auth_device_url, data={
            "client_id": self.client_id, "scope": "openid"})

        if response.status_code != 200:
            print(f"Error in device authorization request: {response.status_code} - {response.text}")
            return

        data = response.json()
        self.device_code = data.get("device_code")
        self.interval = data.get("interval", 5)

        print(f"User code: {data.get('user_code')}")
        print(f"Please authenticate by visiting: {data.get('verification_uri_complete')}")

        if interactive:
            import webbrowser

            webbrowser.open(data.get("verification_uri_complete"))

        # Step 2: Poll for the token
        self.__poll_for_token__()

    def logout(self):
        self.access_token = None
        self.refresh_token = None

    def __poll_for_token__(self):
        token_url = f"{self.idp_url}/realms/{self.realm}/protocol/openid-connect/token"
        print("Waiting for authorization...")
        while True:
            response = requests.post(
                token_url,
                data={
                    "client_id": self.client_id,
                    "grant_type": "urn:ietf:params:oauth:grant-type:device_code",
                    "device_code": self.device_code,
                },
            )
            if response.status_code == 200:
                data = response.json()
                self.refresh_token = data.get("refresh_token")
                self.access_token = data.get("access_token")
                print("Authorization successful!")
                self.__persist_token__()
                return
            elif response.status_code == 400 and response.json().get("error") == "authorization_pending":
                time.sleep(self.interval)
            else:
                print(f"Authorization error: {response.status_code} - {response.text}")
                break

    def __persist_token__(self):
        import json
        with open("auth.state", "w") as f:
            json.dump({"refresh_token": self.refresh_token,
                      "access_token": self.access_token}, f)

    def __load_saved_token__(self):
        import json
        import jwt
        import datetime
        try:
            with open("auth.state", "r") as f:
                data = json.load(f)
                self.refresh_token = str(data["refresh_token"])
                self.access_token = str(data["access_token"])
            decoded = jwt.decode(self.access_token, options={"verify_signature": False})
            tA = datetime.datetime.now(datetime.timezone.utc).timestamp()
            tB = int(decoded.get("exp", 0))
            return tA < tB
        except (FileNotFoundError, KeyError, ValueError, StopIteration) as e:
            print(e)
            return False
