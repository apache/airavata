# Airavata Magic Extension to run local Jupyter Notebooks on Clusters


## Installation

```
pip install airavata_jupyter_magic
```

## Usage

### Load the extension in the notebook
```
%load_ext airavata_jupyter_magic
```

### Initialize the remote cluster job
```
%init_remote cluster=jetstream cpu=2 memory=2GB
```

### Run remote cell
```
%%run_remote
# Your code here
a = 10 + a
print(a)
```

### Terminate the remote cluster job and the connection
```
%terminate_remote
```
