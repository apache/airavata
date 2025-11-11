#!/usr/bin/env python3

import os
import sys
import time
import logging
from pathlib import Path

os.environ['AUTH_SERVER_URL'] = "https://auth.dev.cybershuttle.org"
os.environ['API_SERVER_HOSTNAME'] = "api.dev.cybershuttle.org"
os.environ['GATEWAY_URL'] = "https://gateway.dev.cybershuttle.org"
os.environ['STORAGE_RESOURCE_HOST'] = "gateway.dev.cybershuttle.org"

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from airavata_experiments.airavata import AiravataOperator
from airavata.model.status.ttypes import ExperimentState
from airavata_auth.device_auth import AuthContext

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def list_storage_resources(access_token: str, gateway_id: str | None = None):
    operator = AiravataOperator(access_token=access_token)
    sr_names = operator.api_server_client.get_all_storage_resource_names(operator.airavata_token)
    logger.info("Available storage resources:")
    for sr_id, hostname in sr_names.items():
        logger.info(f"  ID: {sr_id}, Hostname: {hostname}")
    return sr_names


def get_storage_hostname_by_id(access_token: str, storage_resource_id: str) -> str | None:
    operator = AiravataOperator(access_token=access_token)
    sr_names = operator.api_server_client.get_all_storage_resource_names(operator.airavata_token)
    hostname = sr_names.get(storage_resource_id)
    if hostname:
        logger.info(f"Storage ID {storage_resource_id} maps to hostname: {hostname}")
    else:
        logger.warning(f"Storage ID {storage_resource_id} not found in available resources")
    return hostname


def create_and_launch_experiment(
    access_token: str,
    experiment_name: str,
    project_name: str,
    application_name: str,
    computation_resource_name: str,
    queue_name: str,
    node_count: int,
    cpu_count: int,
    walltime: int,
    group_name: str = "Default",
    input_storage_host: str | None = None,
    output_storage_host: str | None = None,
    input_files: dict[str, str | list[str]] | None = None,
    data_inputs: dict[str, str | int | float] | None = None,
    gateway_id: str | None = None,
    auto_schedule: bool = False,
    monitor: bool = True,
) -> dict:
    operator = AiravataOperator(access_token=access_token)
    
    experiment_inputs = {}
    
    if input_files:
        for input_name, file_paths in input_files.items():
            if isinstance(file_paths, list):
                experiment_inputs[input_name] = {
                    "type": "uri[]",
                    "value": [str(Path(fp).resolve()) for fp in file_paths]
                }
                logger.info(f"Added file array input '{input_name}': {file_paths}")
            else:
                experiment_inputs[input_name] = {
                    "type": "uri",
                    "value": str(Path(file_paths).resolve())
                }
                logger.info(f"Added file input '{input_name}': {file_paths}")
    
    if data_inputs:
        for input_name, value in data_inputs.items():
            if isinstance(value, int):
                experiment_inputs[input_name] = {"type": "int", "value": value}
            elif isinstance(value, float):
                experiment_inputs[input_name] = {"type": "float", "value": value}
            else:
                experiment_inputs[input_name] = {"type": "string", "value": str(value)}
            logger.info(f"Added data input '{input_name}': {value}")
    
    if not experiment_inputs:
        logger.info("No inputs provided. Adding dummy input for applications that don't require inputs...")
        experiment_inputs = {"__no_inputs__": {"type": "string", "value": ""}}
    
    logger.info(f"Launching experiment '{experiment_name}'...")
    logger.info(f"  Project: {project_name}")
    logger.info(f"  Application: {application_name}")
    logger.info(f"  Compute Resource: {computation_resource_name}")
    logger.info(f"  Input Storage: {input_storage_host or 'default'}")
    logger.info(f"  Output Storage: {output_storage_host or input_storage_host or 'default'}")
    
    launch_state = operator.launch_experiment(
        experiment_name=experiment_name,
        project=project_name,
        app_name=application_name,
        inputs=experiment_inputs,
        computation_resource_name=computation_resource_name,
        queue_name=queue_name,
        node_count=node_count,
        cpu_count=cpu_count,
        walltime=walltime,
        group=group_name,
        gateway_id=gateway_id,
        input_sr_host=input_storage_host,
        output_sr_host=output_storage_host,
        auto_schedule=auto_schedule,
    )
    
    logger.info(f"Experiment launched successfully!")
    logger.info(f"  Experiment ID: {launch_state.experiment_id}")
    logger.info(f"  Process ID: {launch_state.process_id}")
    logger.info(f"  Experiment Directory: {launch_state.experiment_dir}")
    logger.info(f"  Storage Host: {launch_state.sr_host}")
    
    result = {
        "experiment_id": launch_state.experiment_id,
        "process_id": launch_state.process_id,
        "experiment_dir": launch_state.experiment_dir,
        "storage_host": launch_state.sr_host,
        "mount_point": str(launch_state.mount_point),
    }
    
    if monitor:
        logger.info("Monitoring experiment status...")
        monitor_experiment(operator, launch_state.experiment_id)
    
    return result


def get_experiment_state_value(status) -> tuple[int, str, ExperimentState]:
    """Extract state value, name, and enum from status. Returns (value, name, enum)."""
    if isinstance(status, ExperimentState):
        return status.value, status.name, status
    
    # Handle ExperimentStatus object
    if hasattr(status, 'state'):
        state = status.state
        if isinstance(state, ExperimentState):
            return state.value, state.name, state
        elif hasattr(state, 'value'):
            return state.value, state.name if hasattr(state, 'name') else str(state), state
    
    # Handle direct value/name access
    status_value = status.value if hasattr(status, 'value') else (status if isinstance(status, int) else None)
    status_name = status.name if hasattr(status, 'name') else str(status)
    
    # Convert to ExperimentState enum
    if status_value is not None:
        try:
            enum_state = ExperimentState(status_value)
            return status_value, status_name, enum_state
        except (ValueError, TypeError):
            pass
    
    # Fallback
    return None, status_name, ExperimentState.FAILED


def monitor_experiment(operator: AiravataOperator, experiment_id: str, check_interval: int = 30):
    logger.info(f"Monitoring experiment {experiment_id}...")
    
    while True:
        try:
            status = operator.get_experiment_status(experiment_id)
            status_value, status_name, status_enum = get_experiment_state_value(status)
            logger.info(f"Experiment status: {status_name} (value: {status_value})")
            
            # Check terminal states: COMPLETED (7), CANCELED (6), FAILED (8)
            if status_value is not None:
                is_terminal = status_value in [
                    ExperimentState.COMPLETED.value,  # 7
                    ExperimentState.CANCELED.value,   # 6
                    ExperimentState.FAILED.value      # 8
                ]
            else:
                is_terminal = status_name in ['COMPLETED', 'CANCELED', 'FAILED']
            
            if is_terminal:
                logger.info(f"Experiment finished with state: {status_name}")
                break
        except Exception as e:
            logger.error(f"Error checking experiment {experiment_id} status: {e}")
            import traceback
            logger.debug(traceback.format_exc())
            # Continue monitoring despite errors
        
        time.sleep(check_interval)


def main():
    logger.info("Authenticating...")
    ACCESS_TOKEN = AuthContext.get_access_token()
    
    EXPERIMENT_NAME = "Test"
    PROJECT_NAME = "Default Project"
    APPLICATION_NAME = "NAMD-test"
    GATEWAY_ID = None
    
    COMPUTATION_RESOURCE_NAME = "NeuroData25VC2"
    QUEUE_NAME = "cloud"
    NODE_COUNT = 1
    CPU_COUNT = 1
    WALLTIME = 5
    GROUP_NAME = "Default"
    
    INPUT_STORAGE_HOST = "gateway.dev.cybershuttle.org"
    OUTPUT_STORAGE_HOST = "149.165.169.12"
    
    INPUT_FILES = {}
    DATA_INPUTS = {}
    
    AUTO_SCHEDULE = False
    MONITOR = True
    
    try:
        result = create_and_launch_experiment(
            access_token=ACCESS_TOKEN,
            experiment_name=EXPERIMENT_NAME,
            project_name=PROJECT_NAME,
            application_name=APPLICATION_NAME,
            computation_resource_name=COMPUTATION_RESOURCE_NAME,
            queue_name=QUEUE_NAME,
            node_count=NODE_COUNT,
            cpu_count=CPU_COUNT,
            walltime=WALLTIME,
            group_name=GROUP_NAME,
            input_storage_host=INPUT_STORAGE_HOST,
            output_storage_host=OUTPUT_STORAGE_HOST,
            input_files=INPUT_FILES if INPUT_FILES else None,
            data_inputs=DATA_INPUTS if DATA_INPUTS else None,
            gateway_id=GATEWAY_ID,
            auto_schedule=AUTO_SCHEDULE,
            monitor=MONITOR,
        )
        
        logger.info("\n" + "="*60)
        logger.info("Experiment created and launched successfully!")
        logger.info("="*60)
        logger.info(f"Experiment ID: {result['experiment_id']}")
        logger.info(f"Process ID: {result['process_id']}")
        logger.info(f"Experiment Directory: {result['experiment_dir']}")
        logger.info(f"Storage Host: {result['storage_host']}")
        logger.info("="*60)
        
        return result
        
    except Exception as e:
        logger.error(f"Failed to create/launch experiment: {repr(e)}", exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()
