# Apache Airavata Python SDK

The Apache Airavata Python SDK for third party clients to integrate with Airavata middleware.

All communication uses the unified REST API (`/api/v1/`). Data is exchanged as plain Python dicts (JSON). File operations (list, upload, download) use the File API at `/api/v1/files`; the SDK default `FILE_SVC_URL` is `{API_SERVER_URL}/api/v1/files`. Override `FILE_SVC_URL` if using a different file service base URL.

### Folder Structure
- `airavata_sdk`
  - `clients`
    Integration clients that communicate with the Airavata REST API. Import these to access Airavata middleware.
  - `samples`
    Sample implementations demonstrating integration with Airavata middleware.
  - `transport`
    HTTP connection handling (`RestClient`) with retry logic and session management.
- `airavata_experiments`
  Python APIs to run experiments from anywhere.
  Handles uploading data, running experiments, tracking progress, and fetching data from past runs.
- `airavata_jupyter_magic`
  Jupyter Plugin providing magic annotations (%) to shift notebook runtimes between resources (local/remote).

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
- Create a INI file containing server configuration details. For more information refer to default settings file
  [settings.ini](airavata_sdk/transport/settings.ini)

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
