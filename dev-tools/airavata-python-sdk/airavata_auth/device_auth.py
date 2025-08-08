import os
import time

import requests
from rich.console import Console

from airavata_sdk import Settings

# Load environment variables from .env file


class AuthContext:
    
    @staticmethod
    def get_access_token():
        if os.environ.get("CS_ACCESS_TOKEN", None) is None:
            context = AuthContext()
            context.login()
        return os.environ["CS_ACCESS_TOKEN"]

    def __init__(self):
        self.settings = Settings()
        if not self.settings.AUTH_CLIENT_ID or not self.settings.AUTH_REALM or not self.settings.AUTH_SERVER_URL:
            raise ValueError("Missing required environment variables for client ID, realm, or auth server URL")

        self.device_code = None
        self.interval = None
        self.console = Console()

    def login(self):
        if os.environ.get('CS_ACCESS_TOKEN', None) is not None:
            return
        # Step 1: Request device and user code
        auth_device_url = f"{self.settings.AUTH_SERVER_URL}/realms/{self.settings.AUTH_REALM}/protocol/openid-connect/auth/device"
        response = requests.post(auth_device_url, data={
                                 "client_id": self.settings.AUTH_CLIENT_ID, "scope": "openid"})

        if response.status_code != 200:
            print(
                f"Error in authentication request: {response.status_code} - {response.text}", flush=True)
            return

        data = response.json()
        self.device_code = data.get("device_code")
        self.interval = data.get("interval", 5)

        # Step 2: Poll for the token
        self.poll_for_token(data.get('verification_uri_complete'))

    def poll_for_token(self, url):
        assert self.interval is not None
        token_url = f"{self.settings.AUTH_SERVER_URL}/realms/{self.settings.AUTH_REALM}/protocol/openid-connect/token"
        counter = 0
        with self.console.status(f"Authenticate via link: [link={url}]{url}[/link]", refresh_per_second=1) as status:
            while True:
                response = requests.post(token_url, data={
                    "client_id": self.settings.AUTH_CLIENT_ID,
                    "grant_type": "urn:ietf:params:oauth:grant-type:device_code",
                    "device_code": self.device_code
                })
                if response.status_code == 200:
                    data = response.json()
                    access_token = data.get("access_token")
                    print(f"Authenticated.")
                    os.environ['CS_ACCESS_TOKEN'] = access_token
                    break
                elif response.status_code == 400 and response.json().get("error") == "authorization_pending":
                    counter += 1
                    status.update(
                        f"Authenticate via link: [link={url}]{url}[/link] ({counter})")
                else:
                    print(
                        f"Error during authentication: {response.status_code} - {response.text}")
                    break
                time.sleep(self.interval)
            status.stop()
        self.console.clear()
