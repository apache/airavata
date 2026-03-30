# Airavata Load Client

Python-based load testing and experiment launching tool for Apache Airavata.

## Prerequisites

- Python 3.10+
- `airavata-python-sdk` installed (from `../../airavata-python-sdk/`)
- Dependencies: `pip install -r requirements.txt`

## Modes

### Single Experiment

```bash
python load_client.py single \
  --experiment-name "MyTest" \
  --project "Default Project" \
  --application "Echo" \
  --resource "localhost" \
  --queue "normal" \
  --walltime 5
```

### Batch (N copies of a scenario)

```bash
python load_client.py batch \
  --config load-config.yml \
  --scenario "Echo Test" \
  --copies 10
```

### Load Test (all scenarios from config)

```bash
python load_client.py load --config load-config.yml
```

## Configuration

Edit `load-config.yml` to define scenarios. Each scenario specifies the experiment template, compute resources, inputs, and concurrency parameters.
