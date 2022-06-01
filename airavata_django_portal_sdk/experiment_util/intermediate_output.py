import logging
from typing import List, Union

from airavata.model.data.replica.ttypes import DataProductModel
from airavata.model.experiment.ttypes import ExperimentModel
from airavata.model.job.ttypes import JobModel
from airavata.model.process.ttypes import ProcessModel
from airavata.model.status.ttypes import JobState, ProcessState, ProcessStatus
from airavata.model.task.ttypes import TaskModel, TaskTypes

logger = logging.getLogger(__name__)


def get_intermediate_output_process_status(request, experiment: ExperimentModel, *output_name: str) -> Union[ProcessStatus, None]:
    """Return ProcessStatus of intermediate output fetch process, or None if not available."""
    # check that there is at least one intermediate output fetching process
    output_fetching_processes = _get_output_fetching_processes(experiment)
    if len(output_fetching_processes) > 0:
        try:
            return request.airavata_client.getIntermediateOutputProcessStatus(
                request.authz_token, experiment.experimentId, list(output_name))
        except Exception:
            logger.debug("Failed to get intermediate output process status", exc_info=True)
            return None
    else:
        return None


def can_fetch_intermediate_output(request, experiment: ExperimentModel, output_name: str) -> bool:
    """Return True if intermediate output can be fetched for the given named output."""
    # look at job status and check if currently running intermediate output process
    jobs: List[JobModel] = []
    process: ProcessModel
    task: TaskModel
    for process in experiment.processes:
        for task in process.tasks:
            for job in task.jobs:
                jobs.append(job)

    def latest_status_is_active(job: JobModel) -> bool:
        if not job.jobStatuses or len(job.jobStatuses) == 0:
            return False
        return job.jobStatuses[-1].jobState == JobState.ACTIVE
    # If there are no active jobs, return False
    if not any(map(latest_status_is_active, jobs)):
        return False

    try:
        # Return True if process status is in a terminal state
        process_status = get_intermediate_output_process_status(request, experiment, output_name)
        return process_status.state in [ProcessState.CANCELED, ProcessState.COMPLETED, ProcessState.FAILED]
    except Exception:
        # Return True since error here likely means that there is no currently running process
        return True


def fetch_intermediate_output(request, experiment_id: str, *output_name: str) -> None:
    """Start a fetch of the output file for a currently running experiment."""
    request.airavata_client.fetchIntermediateOutputs(
        request.authz_token, experiment_id, list(output_name))


def get_intermediate_output_data_products(request, experiment: ExperimentModel, output_name: str) -> List[DataProductModel]:
    """Return the DataProduct instance(s) for a experiment output."""
    output_fetching_processes = _get_output_fetching_processes(experiment)

    data_products = []
    if (len(output_fetching_processes) > 0):
        most_recent_completed_process_output = None
        for process in output_fetching_processes:
            # Skip over any processes that aren't completed
            if (len(process.processStatuses) == 0 or process.processStatuses[-1].state != ProcessState.COMPLETED):
                continue
            for process_output in process.processOutputs:
                if process_output.name == output_name:
                    most_recent_completed_process_output = process_output
                    break
            if most_recent_completed_process_output is not None:
                break
        if most_recent_completed_process_output is not None:
            data_product_uris = []
            if most_recent_completed_process_output.value.startswith('airavata-dp://'):
                data_product_uris = most_recent_completed_process_output.value.split(',')
            for data_product_uri in data_product_uris:
                data_product = request.airavata_client.getDataProduct(
                    request.authz_token, data_product_uri)
                data_products.append(data_product)
    return data_products


def _get_output_fetching_processes(experiment: ExperimentModel) -> List[ProcessModel]:
    "sort the processes (most recent first) and filter to just the output fetching ones"
    processes: List[ProcessModel] = sorted(experiment.processes, key=lambda p: p.creationTime, reverse=True) if experiment.processes else []
    output_fetching_processes: List[ProcessModel] = []
    for process in processes:
        if any(map(lambda t: t.taskType == TaskTypes.OUTPUT_FETCHING, process.tasks)):
            output_fetching_processes.append(process)
    return output_fetching_processes
