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

import logging
import time
from typing import Any, Optional

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

from airavata_sdk import Settings

log = logging.getLogger(__name__)

settings = Settings()


class RestClient:
    """HTTP REST client for the Airavata API server.

    Handles authentication, retries, and JSON request/response serialization.
    All Airavata services (experiments, applications, credentials, groups, etc.)
    are accessed through the unified REST API at /api/v1/.
    """

    def __init__(
        self,
        access_token: str,
        base_url: Optional[str] = None,
        verify_ssl: Optional[bool] = None,
        max_retries: Optional[int] = None,
        retry_delay: Optional[float] = None,
    ):
        self.base_url = (base_url or settings.API_SERVER_URL).rstrip("/")
        self.api_url = f"{self.base_url}/api/v1"
        self.access_token = access_token
        self.verify_ssl = verify_ssl if verify_ssl is not None else settings.VERIFY_SSL
        self.max_retries = max_retries or settings.CONNECTION_MAX_RETRIES
        self.retry_delay = retry_delay or settings.CONNECTION_RETRY_DELAY

        self.session = requests.Session()
        self.session.headers.update({
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        })
        self.session.verify = self.verify_ssl

        retry_strategy = Retry(
            total=self.max_retries,
            backoff_factor=self.retry_delay,
            status_forcelist=[502, 503, 504],
            allowed_methods=["GET", "POST", "PUT", "DELETE"],
        )
        adapter = HTTPAdapter(max_retries=retry_strategy)
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)

    def _url(self, path: str) -> str:
        return f"{self.api_url}/{path.lstrip('/')}"

    def _handle_response(self, response: requests.Response) -> Any:
        response.raise_for_status()
        if response.status_code == 204 or not response.content:
            return None
        return response.json()

    def get(self, path: str, params: Optional[dict] = None) -> Any:
        response = self.session.get(self._url(path), params=params)
        return self._handle_response(response)

    def post(self, path: str, json: Any = None, params: Optional[dict] = None) -> Any:
        response = self.session.post(self._url(path), json=json, params=params)
        return self._handle_response(response)

    def put(self, path: str, json: Any = None, params: Optional[dict] = None) -> Any:
        response = self.session.put(self._url(path), json=json, params=params)
        return self._handle_response(response)

    def delete(self, path: str, params: Optional[dict] = None) -> Any:
        response = self.session.delete(self._url(path), params=params)
        return self._handle_response(response)

    def health_check(self) -> dict:
        return self.get("/health")

    def close(self):
        self.session.close()
