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
    # API Server Connection Settings
    # ------------------------------------------------------------

    @property
    def API_SERVER_URL(self):
        return str(os.getenv("API_SERVER_URL", "https://api.gateway.cybershuttle.org"))

    @property
    def VERIFY_SSL(self):
        return bool(os.getenv("VERIFY_SSL", True))

    @property
    def CONNECTION_MAX_RETRIES(self):
        return int(os.getenv("CONNECTION_MAX_RETRIES", 3))

    @property
    def CONNECTION_RETRY_DELAY(self):
        return float(os.getenv("CONNECTION_RETRY_DELAY", 1.0))

    @property
    def MONITOR_STATUS(self):
        return bool(os.getenv("MONITOR_STATUS", False))

    # ------------------------------------------------------------
    # File Service Connection Settings
    # ------------------------------------------------------------

    @property
    def FILE_SVC_URL(self):
        """Base URL for File API (list, upload, download). Default: same server at /api/v1/files."""
        return str(os.getenv("FILE_SVC_URL", f"{self.API_SERVER_URL}/api/v1/files"))

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
        return str(os.getenv("STORAGE_RESOURCE_HOST", "gateway.cybershuttle.org"))

    @property
    def SFTP_PORT(self):
        return int(os.getenv("SFTP_PORT", 9000))
