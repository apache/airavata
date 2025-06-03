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

import datetime
import json
import os
import time
import webbrowser

import jwt
import requests


class DeviceFlowAuthenticator:

    idp_url: str
    realm: str
    client_id: str
    interval: int
    device_code: str | None
    _access_token: str | None
    _refresh_token: str | None

    def __has_expired__(self, token: str) -> bool:
      try:
          decoded = jwt.decode(token, options={"verify_signature": False})
          tA = datetime.datetime.now(datetime.timezone.utc).timestamp()
          tB = int(decoded.get("exp", 0))
          return tA >= tB
      except:
          return True

    @property
    def access_token(self) -> str:
      if self._access_token and not self.__has_expired__(self._access_token):
        return self._access_token
      elif self._refresh_token and not self.__has_expired__(self._refresh_token):
        self.refresh()
      else:
         self.login()
      assert self._access_token
      return self._access_token
    
    @property
    def refresh_token(self) -> str:
      if self._refresh_token and not self.__has_expired__(self._refresh_token):
        return self._refresh_token
      else:
        self.login()
      assert self._refresh_token
      return self._refresh_token
       

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

        self.interval = 5
        self.device_code = None
        self._access_token = None
        self._refresh_token = None

    def refresh(self) -> None:
        auth_device_url = f"{self.idp_url}/realms/{self.realm}/protocol/openid-connect/token"
        response = requests.post(auth_device_url, data={
            "client_id": self.client_id,
            "grant_type": "refresh_token",
            "scope": "openid",
            "refresh_token": self._refresh_token
        })
        if response.status_code != 200:
            raise Exception(f"Error in token refresh request: {response.status_code} - {response.text}")
        data = response.json()
        self._refresh_token = data["refresh_token"]
        self._access_token = data["access_token"]
        assert self._access_token is not None
        assert self._refresh_token is not None
        self.__persist_token__(self._refresh_token, self._access_token)

    def login(self, interactive: bool = True) -> None:
        auth_warning = None
        try:
          # [Flow A] Reuse saved token
          if os.path.exists("auth.state"):
            try:
              # [A1] Load token from file
              with open("auth.state", "r") as f:
                  data = json.load(f)
              self._refresh_token = str(data["refresh_token"])
              self._access_token = str(data["access_token"])
            except:
              auth_warning = "Failed to load auth.state file!"
            else:
              # [A2] Check if access token is valid, if so, return
              if not self.__has_expired__(self._access_token):
                return print("Authenticated via saved access token!")
              else:
                auth_warning = "Access token is invalid!"
              # [A3] Check if refresh token is valid. if so, refresh
              try:
                if not self.__has_expired__(self._refresh_token):
                  self.refresh()
                  return print("Authenticated via saved refresh token!")
                else:
                  auth_warning = "Refresh token is invalid!"
              except Exception as e:
                print(*e.args)
          if auth_warning:
             print(auth_warning)
            
          # [Flow B] Request device and user code

          # [B1] Initiate device auth flow
          auth_device_url = f"{self.idp_url}/realms/{self.realm}/protocol/openid-connect/auth/device"
          response = requests.post(auth_device_url, data={
              "client_id": self.client_id,
              "scope": "openid",
          })
          if response.status_code != 200:
              raise Exception(f"Error in device authorization request: {response.status_code} - {response.text}")
          data = response.json()
          self.device_code = data.get("device_code", self.device_code)
          self.interval = data.get("interval", self.interval)
          url = data['verification_uri_complete']
          print(f"Please authenticate by visiting: {url}")
          if interactive:
              webbrowser.open(url)
          
          # [B2] Poll until token is received
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
                  self.__persist_token__(data["refresh_token"], data["access_token"])
                  print("Authenticated via device auth!")
                  return
              elif response.status_code == 400 and response.json().get("error") == "authorization_pending":
                  time.sleep(self.interval)
              else:
                  raise Exception(f"Authorization error: {response.status_code} - {response.text}")
        
        except Exception as e:
          print("login() failed!", e)

    def logout(self) -> None:
        self._access_token = None
        self._refresh_token = None

    def __persist_token__(self, refresh_token: str, access_token: str) -> None:
        self._access_token = access_token
        self._refresh_token = refresh_token
        import json
        with open("auth.state", "w") as f:
            json.dump({"refresh_token": self._refresh_token, "access_token": self._access_token}, f)
