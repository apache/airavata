# Apache Airavata Python SDK

The Apache Airavata Python SDK for third party clients to integrate with Airavata middleware

### Folder Structure
- `airavata`
  Includes gRPC generated stubs (protobuf models and service clients).
  Mainly contains data model and functions.
  You may need to import data model packages to integrate with clients.
- `airavata_sdk`
  - `clients`
    Includes integration clients, which you want to import and integrate with your code to access airavata middleware.
  - `samples`
    Includes set of sample implementation of integration clients to demonstrate the integration with airavata middleware
  - `transport`
    Includes connection handling classes for gRPC channels
- `airavata_experiments`
  Python APIs to run experiments from anywhere.
  Handles uploading data, running experiments, tracking progress, and fetching data from past runs.
- `airavata_jupyter_magic`
  Jupyter Plugin providing magic annotations (%) to shift notebook runtimes between resources (local/remote).

### Connection Model

The SDK connects to Airavata via **gRPC** (using `grpcio`) on a single port (default `9090`).
All API services are served by an Armeria server that provides both gRPC and REST (HTTP/JSON transcoding).

The default server settings are in `airavata_sdk/__init__.py` (`Settings` class) and can be overridden
with environment variables (e.g., `API_SERVER_HOSTNAME`, `API_SERVER_PORT`).

### Before Integration

- Create a virtual environment
  ```bash
  python3 -m venv venv
  ```
- Activate the virtual environment
  ```bash
  source venv/bin/activate
  ```
- Install dependencies
  ```bash
  pip install -e .
  ```
- Configure server connection via environment variables or the `Settings` class defaults:
  - `API_SERVER_HOSTNAME` (default: `api.gateway.cybershuttle.org`)
  - `API_SERVER_PORT` (default: `9090`)
  - `AUTH_SERVER_URL`, `AUTH_REALM`, `AUTH_CLIENT_ID` for Keycloak authentication

### Generating Distribution Archives (Optional)

You can generate a `*.tar.gz` distribution and install to any external project.
- Make sure you have the latest versions of `build` and `setuptools` installed
  ```bash
  python3 -m pip install --upgrade build setuptools
  ```
- Now run this command from the same directory where `pyproject.toml` is located
  ```bash
  python3 -m build .
  ```
- This command should output a lot of text and once completed should generate two files in the dist directory
  ```
  dist/
      airavata_python_sdk-2.0.0-py2.py3-none-any.whl
      airavata-python-sdk-2.0.0.tar.gz
  ```

Now, you should be able to install those packages into your project.