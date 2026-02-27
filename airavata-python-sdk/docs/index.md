# Airavata Python SDK

The Apache Airavata Python SDK lets third-party clients interact with Airavata to run scientific experiments. It provides declarative APIs to submit and manage experiments, and internally handles the complexities of deploying, running, and connecting to scientific apps on HPC resources.

## Main APIs
* **Airavata Experiments** - Run scientific apps, use data/results from past runs.
* **Airavata Jupyter Magic** - Switch runtimes, move data, run experiments/analyses, all from within a notebook.
* **Airavata SDK** - Create research groups, manage resource allocations, and setup scientific apps on different HPC resources.

## Project Layout

```mermaid
flowchart TB
    subgraph Root["airavata_python_sdk/"]
        subgraph airavata["airavata/"]
            a_api["api/"]
            a_base["base/"]
            a_model["model/"]
            a_service["service/"]
            a_init["__init__.py"]
        end
        subgraph experiments["airavata_experiments/"]
            e_md["md/"]
            e_neuro["neuro/"]
            e_init["__init__.py"]
            e_py["airavata.py, base.py, plan.py, runtime.py, scripter.py, sftp.py, task.py"]
        end
        subgraph jupyter["airavata_jupyter_magic/"]
            j_init["__init__.py"]
        end
        subgraph auth["airavata_auth/"]
            auth_device["device_auth.py"]
        end
        subgraph sdk["airavata_sdk/"]
            s_clients["clients/"]
            s_samples["samples/"]
            s_transport["transport/"]
            s_init["__init__.py"]
        end
    end
```