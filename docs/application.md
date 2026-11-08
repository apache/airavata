# Applications

An **application** in Airavata is a program a user can run on a compute resource without writing a job script, such as AlphaFold2, GROMACS or an in-house analysis code. Airavata describes it in three layers, and each answers a different question:

| Layer | Question it answers | Resource |
|---|---|---|
| **Application template** | *What* does the application take in and produce? | `application-templates` |
| **Application deployment** | *How* is it run on a particular compute resource? | `slurm-deployments` (Slurm backend) |
| **Process** | *This* run: which values, which data, which resources? | `processes` |

Any authenticated user can create all three.

```
Application template  ──1:N──▶  Application deployment  ──1:N──▶  Process (one run)
  inputs[], outputs[]             compute resource, run section,    inputMappings[], outputMappings[],
                                  default resources                 resource request, credentials
```

The template is the application's contract. It names every input and output and gives each one a type, and it says nothing about where or how the program runs. A deployment binds that contract to one compute resource: the commands that launch the program there and the resources a run asks for by default. A process fills in the contract for one run, giving a value for each input and a destination for each output, and launches it.

Keeping these apart means that one template can be deployed on several compute resources, and one deployment can serve every user who has access to its resource. Slurm clusters are the compute backend supported today. A deployment is the layer that changes when other backends are added. Templates and the way runs supply inputs and outputs stay the same.

File-like inputs and outputs are passed as **data products**, which are registered datasets on a storage host. [data-product.md](data-product.md) explains what they are and how to set them up.

The endpoints and full request and response bodies are in [api.md](api.md). See [Application Templates](api.md#application-templates), [Batch Deployments](api.md#batch-deployments) and [Processes](api.md#processes). This page explains the model behind them.

---

## Application template

A template has a name, an optional description, and two lists of declarations:

```json
{
  "templateName": "AlphaFold2",
  "templateDescription": "AlphaFold2 protein structure prediction application",
  "inputs":  [ /* input declarations */ ],
  "outputs": [ /* output declarations */ ]
}
```

Inputs and outputs belong to the template. They are created, replaced and deleted with it, and an update replaces the whole list, which gives every declaration a new `inputId`/`outputId`. A template that still has deployments cannot be deleted (`409 Conflict`).

### Input declarations

| Field | Required | Meaning |
|---|---|---|
| `inputName` | yes | The input's identifier. It must be unique within the template, and the run section refers to the input by it (`{{ inputs.<inputName>[0] }}`). Use a name that is also a valid identifier (`[A-Za-z_][A-Za-z0-9_]*`) so that it works in the template syntax. |
| `inputType` | yes | One of the [input types](#input-types) below. |
| `displayName` | no | A label for user interfaces. |
| `inputDescription` | no | Help text for whoever fills the input in. |
| `required` | no | Whether a run must supply a value (default `false`). |
| `defaultValue` | no | The value used when a run gives none. It is a JSON document; see [Value encoding](#value-encoding). |

### Input types

Inputs fall into two families. **Scalar** inputs are passed to the program as literal text. **Data** inputs name [data products](data-product.md). Airavata stages them into the run's work directory before the job starts, and the run section sees the paths of the staged copies.

| Type | Family | What the run supplies | What the run section sees |
|---|---|---|---|
| `STRING` | scalar | any text, e.g. `{"value":"monomer"}` | the text as given |
| `INTEGER` | scalar | a whole number, e.g. `{"value":300}` | the number as text, e.g. `300` |
| `FLOAT` | scalar | a decimal number, e.g. `{"value":0.05}` | the number as text |
| `BOOLEAN` | scalar | `true` or `false`, e.g. `{"value":true}` | `true` / `false` |
| `FILE` | data | the id of one **file** data product | the path of the staged file |
| `FILE_LIST` | data | a list of ids of **file** data products | one path per product, each a staged file |
| `DIRECTORY` | data | the id of one **directory** data product | the path of the staged directory |

A data product records whether it is a file or a directory (`isFile`). A `FILE` or `FILE_LIST` input takes file products only, and a `DIRECTORY` input takes a directory product only.

### Output declarations

| Field | Required | Meaning |
|---|---|---|
| `outputName` | yes | The output's identifier. It is also the **name** the job is expected to write in its work directory. |
| `outputType` | yes | One of the [output types](#output-types) below. |
| `displayName` | no | A label for user interfaces. |
| `outputDescription` | no | Help text. |

Outputs have no value and no default. The application produces them, so a run only says *where each output should go*, by naming a data product for it.

### Output types

Every output is data. An application's result is always something it writes to disk.

| Type | Meaning | Where the job writes it | Mapped to |
|---|---|---|---|
| `FILE` | a single file | `<workDir>/<outputName>` | a **file** data product |
| `FILE_LIST` | several files collected together | files under `<workDir>/<outputName>/` | a **directory** data product the files are collected into |
| `DIRECTORY` | a directory tree | `<workDir>/<outputName>/` | a **directory** data product |

After the job finishes, Airavata copies each mapped output from the work directory to its data product's path.

Because an output's name is its file or directory name, the output name is part of the contract between Airavata and the program. A program that writes `confidence_scores.json` needs an output named `confidence_scores.json`, or a run section that passes `{{ outputs.confidenceScores }}` to the program as its output path, as shown [below](#the-run-section).

### Value encoding

A default value (`defaultValue`) and a run's mapping value (`value`) are strings that hold a small JSON document:

| Form | Use for | Example |
|---|---|---|
| `{"value": <x>}` | a single value | `"{\"value\":\"monomer\"}"`, `"{\"value\":300}"` |
| `{"values": [ ... ]}` | a list (`FILE_LIST`) | `"{\"values\":[\"<dataId 1>\",\"<dataId 2>\"]}"` |
| `[ ... ]` | a list, short form | `"[\"<dataId 1>\",\"<dataId 2>\"]"` |
| bare text | a single value, usually a data product id for `FILE` or `DIRECTORY` | `"7a8b9c0d-…"` |

Every form becomes a list of strings internally, which is why the run section indexes inputs with `[0]`. A `{"value": null}` document produces an empty list.

---

## Application deployment

A deployment makes a template runnable on one compute resource. For the Slurm backend it carries:

| Field | Meaning |
|---|---|
| `templateId` | The template being deployed. |
| `slurmClusterId` | The cluster it runs on. This names only the machine. The account and SSH key a run submits under come from a [cluster config](api.md#slurm-cluster-configs) that the run chooses. |
| `slurmRunSection` | The commands that run the application: module loads, the program invocation and any cleanup. It is a Jinja template ([pongo2](https://github.com/flosch/pongo2)). |
| `defaultBatchJobConfig` | The resource request a run starts from: wall time, allocation, nodes, CPUs, memory, GPUs and constraints. A run copies it and may change any of it. |
| `defaultPartition` | The Slurm partition to submit to. |

### The run section

Airavata generates the job header (`#SBATCH` lines on Slurm) itself. The run section is the body of the script, and it reaches the run's inputs and outputs through two variables:

| Variable | Type | Contents |
|---|---|---|
| `inputs` | name → list of strings | every input the run mapped, after defaults are applied. Data inputs are given as paths in the work directory. |
| `outputs` | name → string | `<workDir>/<outputName>` for every output the run mapped |

The usual patterns:

```jinja
module load alphafold

run_alphafold.sh \
  --fasta_paths={{ inputs.fastaFile[0] }} \
  --model_preset={{ inputs.modelPreset[0] }} \
  --extra_pdbs {{ inputs.templates|join:" " }} \
  --output_dir={{ outputs.rankedModels }} \
  --scores_out={{ outputs.confidenceScores }}
```

- A single-valued input (any scalar, `FILE` or `DIRECTORY`): `{{ inputs.name[0] }}`
- A `FILE_LIST`: `{{ inputs.name|join:" " }}` expands to every path, separated by spaces.
- An output: `{{ outputs.name }}`. Use `{{ outputs["name.ext"] }}` if the name contains a dot.
- Optional inputs can be guarded: `{% if inputs.seed %}--seed {{ inputs.seed[0] }}{% endif %}`

Other variables are available too: `work_dir`, `work_dir_quoted`, `process_id`, `job_name`, `stdout_file`, `stderr_file`, and the resource values (`nodes`, `cpus`, `mem`, `gpus`, …).

Shell-style references such as `$fastaFile` are **not** substituted by Airavata. They reach the script unchanged and are expanded by bash as ordinary environment variables, which are usually empty.

The run section is rendered first and its output is inserted into the script as plain text. A value a user supplies for an input is therefore never interpreted as template code.

---

## Process: running an application

A `BATCH_JOB` process is one run of a deployment. Its `batchProcess` section names the deployment, the cluster config to submit under, the `baseWorkDir`, its own `batchJobConfig`, and two mapping lists that fill in the template:

```json
"inputMappings": [
  { "templateInputId": "<fastaFile inputId>",   "value": "<file data product id>" },
  { "templateInputId": "<templates inputId>",   "value": "{\"values\":[\"<id 1>\",\"<id 2>\"]}" },
  { "templateInputId": "<modelPreset inputId>", "value": "{\"value\":\"monomer\"}" }
],
"outputMappings": [
  { "templateOutputId": "<rankedModels outputId>",     "value": "<directory data product id>" },
  { "templateOutputId": "<confidenceScores outputId>", "value": "<file data product id>" }
]
```

Mappings refer to declarations by **id** (`inputId` / `outputId`), not by name. Read the ids back from `GET /api/v1/application-templates/{templateId}`.

| Declaration type | Mapping `value` holds |
|---|---|
| scalar input | the value, in the [encoded form](#value-encoding) |
| `FILE` input | the id of a file data product to copy in |
| `FILE_LIST` input | the ids of the file data products to copy in |
| `DIRECTORY` input | the id of a directory data product to copy in |
| `FILE` output | the id of a file data product to copy the result to. The path need not exist yet. |
| `FILE_LIST` / `DIRECTORY` output | the id of a directory data product to copy the results into |

A product's storage supplies the host and account each copy goes through. See [data-product.md](data-product.md).

### What happens at launch

The run works in `<baseWorkDir>/<processId>` on the cluster. If the run gives no `baseWorkDir`, the cluster config's work root is used. Launching creates tasks in this order:

1. **Stage in.** Airavata copies each data input's products from their storage into the work directory.
2. **Submit.** Airavata renders the script with `inputs` and `outputs` as described above and submits it.
3. **Monitor.** Airavata tracks the job until it finishes.
4. **Stage out.** Airavata copies each mapped output from the work directory to its data product's path.

Staging tasks retry up to three times on failure. An output the run does not map is not staged and stays in the work directory on the cluster.

### Defaults

A default applies when the run includes a mapping for that input with no value (`"value": null`). An input the run leaves out of `inputMappings` entirely does not appear in `inputs`, even if it has a default, so clients should send a mapping for every declared input. The [MCP server](mcp.md)'s run planner does this, filling in defaults and checking `required` inputs and scalar types before it creates the run.

---

## Complete example

A template for a tool that takes a structure file, an iteration count, an optional list of extra files and a parameter directory, and writes one result file:

```json
{
  "templateName": "Relax",
  "inputs": [
    { "inputName": "structure",  "inputType": "FILE",      "required": true },
    { "inputName": "iterations", "inputType": "INTEGER",   "required": false, "defaultValue": "{\"value\":100}" },
    { "inputName": "extras",     "inputType": "FILE_LIST", "required": false },
    { "inputName": "params",     "inputType": "DIRECTORY", "required": true }
  ],
  "outputs": [
    { "outputName": "relaxed.pdb", "outputType": "FILE" }
  ]
}
```

Its deployment's run section:

```jinja
module load relax
relax --in {{ inputs.structure[0] }} \
      --iterations {{ inputs.iterations[0] }} \
      --params {{ inputs.params[0] }} \
      {% if inputs.extras %}--extras {{ inputs.extras|join:" " }}{% endif %} \
      --out {{ outputs["relaxed.pdb"] }}
```

A run maps:

- `structure` to a file product
- `iterations` to `null`, so the default of 100 applies
- `params` to a directory product
- `extras` to two file products
- `relaxed.pdb` to a file product on the user's lab storage

Airavata stages the four input products into the work directory and renders the run section with their staged paths. The job writes `relaxed.pdb` into the work directory, and Airavata then stages it out to the lab storage.

For the full walkthrough with `curl` (registering storages and data products, creating the process and launching it) see [api.md](api.md#processes).
