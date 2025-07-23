#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import os


class Settings:
    """
    Settings for the Airavata Python SDK

    """

    # ------------------------------------------------------------
    # Keycloak Connection Settings
    # ------------------------------------------------------------

    @property
    def AUTH_CLIENT_ID(self):
        return str(os.getenv("AUTH_CLIENT_ID", "cybershuttle-agent"))

    @property
    def AUTH_REALM(self):
        return str(os.getenv("AUTH_REALM", "default"))

    @property
    def AUTH_SERVER_URL(self):
        return str(os.getenv("AUTH_SERVER_URL", "https://auth.cybershuttle.org"))

    # ------------------------------------------------------------
    # Thrift Connection Settings
    # ------------------------------------------------------------

    @property
    def THRIFT_CLIENT_POOL_KEEPALIVE(self):
        return int(os.getenv("THRIFT_CLIENT_POOL_KEEPALIVE", 5))

    @property
    def VERIFY_SSL(self):
        return bool(os.getenv("VERIFY_SSL", True))

    @property
    def THRIFT_CONNECTION_MAX_RETRIES(self):
        return int(os.getenv("THRIFT_CONNECTION_MAX_RETRIES", 3))

    @property
    def THRIFT_CONNECTION_RETRY_DELAY(self):
        return float(os.getenv("THRIFT_CONNECTION_RETRY_DELAY", 1.0))

    # ------------------------------------------------------------
    # API Server Connection Settings
    # ------------------------------------------------------------

    @property
    def API_SERVER_HOSTNAME(self):
        return str(os.getenv("API_SERVER_HOSTNAME", "api.gateway.cybershuttle.org"))

    @property
    def API_SERVER_PORT(self):
        return int(os.getenv("API_SERVER_PORT", 8930))

    @property
    def API_SERVER_URL(self):
        return str(os.getenv("API_SERVER_URL", f"https://{self.API_SERVER_HOSTNAME}"))

    @property
    def API_SERVER_SECURE(self):
        return bool(os.getenv("API_SERVER_SECURE", False))

    @property
    def MONITOR_STATUS(self):
        return bool(os.getenv("MONITOR_STATUS", False))

    # ------------------------------------------------------------
    # File Service Connection Settings
    # ------------------------------------------------------------

    @property
    def FILE_SVC_URL(self):
        return str(os.getenv("FILE_SVC_URL", f"http://{self.API_SERVER_HOSTNAME}:8050"))

    # ------------------------------------------------------------
    # Profile Service Connection Settings
    # ------------------------------------------------------------

    @property
    def PROFILE_SERVICE_HOST(self):
        return str(os.getenv("PROFILE_SERVICE_HOST", self.API_SERVER_HOSTNAME))

    @property
    def PROFILE_SERVICE_PORT(self):
        return int(os.getenv("PROFILE_SERVICE_PORT", 8962))

    @property
    def PROFILE_SERVICE_SECURE(self):
        return bool(os.getenv("PROFILE_SERVICE_SECURE", False))

    # ------------------------------------------------------------
    # Sharing Service Connection Settings
    # ------------------------------------------------------------

    @property
    def SHARING_API_HOST(self):
        return str(os.getenv("SHARING_API_HOST", self.API_SERVER_HOSTNAME))

    @property
    def SHARING_API_PORT(self):
        return int(os.getenv("SHARING_API_PORT", 7878))

    @property
    def SHARING_API_SECURE(self):
        return bool(os.getenv("SHARING_API_SECURE", False))

    # ------------------------------------------------------------
    # Credential Store Connection Settings
    # ------------------------------------------------------------

    @property
    def CREDENTIAL_STORE_API_HOST(self):
        return str(os.getenv("CREDENTIAL_STORE_API_HOST", self.API_SERVER_HOSTNAME))

    @property
    def CREDENTIAL_STORE_API_PORT(self):
        return int(os.getenv("CREDENTIAL_STORE_API_PORT", 8960))

    @property
    def CREDENTIAL_STORE_API_SECURE(self):
        return bool(os.getenv("CREDENTIAL_STORE_API_SECURE", False))

    # ------------------------------------------------------------
    # Gateway Settings
    # ------------------------------------------------------------

    @property
    def GATEWAY_ID(self):
        return str(os.getenv("GATEWAY_ID", "default"))

    @property
    def GATEWAY_URL(self):
        return str(os.getenv("GATEWAY_URL", "https://gateway.cybershuttle.org"))

    @property
    def GATEWAY_DATA_STORE_DIR(self):
        return str(os.getenv("GATEWAY_DATA_STORE_DIR", f"/var/www/portals/gateway-user-data/{self.GATEWAY_ID}"))

    # ------------------------------------------------------------
    # Storage Settings
    # ------------------------------------------------------------

    @property
    def STORAGE_RESOURCE_HOST(self):
        return str(os.getenv("STORAGE_RESOURCE_HOST", "cybershuttle.org"))

    @property
    def SFTP_PORT(self):
        return int(os.getenv("SFTP_PORT", 9000))
