# Job Status REST Server (Legacy)

> **Note:** This is a legacy utility. The main Airavata server now uses Dapr for job status messaging. See the main [README](../../README.md) for current architecture.

A simple Flask REST API for sending job status email notifications from HPC clusters.

## Overview

This utility provides a REST endpoint that clusters can call to send job completion emails. It was designed for scenarios where direct email sending from compute nodes is restricted.

## Requirements

```bash
pip install flask flask-restful
```

## Configuration

Edit `app.py` to configure:
- `email_username` - Gmail SMTP username
- `email_password` - Gmail SMTP password (use app-specific password)
- `from_address` - Sender email address

## Usage

Start the server:
```bash
python app.py
```

Send a job status notification:
```bash
curl -X POST -d "job_id=ABC&job_name=Test&status=0&emails[]=user@example.com" http://localhost:8089/job_status
```

## API

### POST /job_status

Send job completion notification.

**Parameters:**
- `job_id` - Job identifier
- `job_name` - Job name
- `status` - Job status (0 = completed)
- `emails[]` - List of recipient email addresses

**Response:** `{"result": 200}`