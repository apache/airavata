#!/usr/bin/env python3

import os
import sys
import time
import logging
from concurrent.futures import ThreadPoolExecutor
from typing import List, Dict, Any, Union
import pydantic
from rich.progress import Progress, BarColumn, TextColumn, SpinnerColumn, TimeElapsedColumn
from rich.console import Console
from rich.live import Live
from rich.panel import Panel
from rich.layout import Layout
from collections import deque

os.environ['AUTH_SERVER_URL'] = "https://auth.dev.cybershuttle.org"
os.environ['API_SERVER_HOSTNAME'] = "api.dev.cybershuttle.org"
os.environ['GATEWAY_URL'] = "https://gateway.dev.cybershuttle.org"
os.environ['STORAGE_RESOURCE_HOST'] = "gateway.dev.cybershuttle.org"

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from create_launch_experiment_with_storage import create_and_launch_experiment
from airavata_experiments.airavata import AiravataOperator
from airavata.model.status.ttypes import ExperimentState


class ExperimentLaunchResult(pydantic.BaseModel):
    """Result from creating and launching an experiment."""
    experiment_id: str
    process_id: str
    experiment_dir: str
    storage_host: str
    mount_point: str


class JobConfig(pydantic.BaseModel):
    """Configuration for a batch job submission."""
    experiment_name: str
    project_name: str
    application_name: str
    computation_resource_name: str
    queue_name: str
    node_count: int
    cpu_count: int
    walltime: int
    group_name: str = "Default"
    input_storage_host: str | None = None
    output_storage_host: str | None = None
    input_files: Dict[str, Union[str, List[str]]] | None = None
    data_inputs: Dict[str, Union[str, int, float]] | None = None
    gateway_id: str | None = None
    auto_schedule: bool = False


class JobResult(pydantic.BaseModel):
    """Result from submitting and monitoring a single job."""
    job_index: int
    experiment_id: str | None
    status: str
    result: ExperimentLaunchResult | None = None
    success: bool
    error: str | None = None


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


def monitor_experiment_silent(operator: AiravataOperator, experiment_id: str, check_interval: int = 30) -> ExperimentState:
    """Monitor experiment silently until completion. Returns final status."""
    logger = logging.getLogger(__name__)
    max_checks = 3600  # Maximum number of checks (about 5 hours at 5s interval)
    check_count = 0
    
    # Use shorter interval initially, then increase
    initial_interval = min(check_interval, 5)  # Check every 5 seconds initially
    
    while check_count < max_checks:
        try:
            status = operator.get_experiment_status(experiment_id)
            
            # Extract state information
            status_value, status_name, status_enum = get_experiment_state_value(status)
            
            # Log status periodically for debugging
            if check_count % 12 == 0:  # Log every minute (12 * 5s)
                logger.debug(f"Experiment {experiment_id} status check {check_count}: value={status_value}, name={status_name}")
            
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
                logger.info(f"Experiment {experiment_id} reached terminal state: {status_name} (value: {status_value})")
                return status_enum
            
        except Exception as e:
            # If we can't get status, log but continue monitoring
            logger.warning(f"Error checking experiment {experiment_id} status (check {check_count}): {e}")
            import traceback
            logger.debug(traceback.format_exc())
            if check_count > 10:  # After several failed checks, assume failed
                logger.error(f"Multiple status check failures for {experiment_id}, assuming FAILED")
                return ExperimentState.FAILED
        
        # Sleep before next check
        sleep_time = initial_interval if check_count < 6 else check_interval
        time.sleep(sleep_time)
        check_count += 1
    
    # If we've exceeded max checks, assume failed
    logger.error(f"Experiment {experiment_id} monitoring timeout after {check_count} checks, assuming FAILED")
    return ExperimentState.FAILED


def submit_and_monitor_job(
    job_index: int,
    job_config: JobConfig | Dict[str, Any],
    access_token: str,
) -> JobResult:
    """Submit and monitor a single job. Returns job result with status."""
    # Convert dict to JobConfig if needed
    if isinstance(job_config, dict):
        job_config = JobConfig(**job_config)
    
    try:
        # Make experiment name unique for each job to avoid directory conflicts
        # Using job_index ensures uniqueness and makes it easy to track
        unique_experiment_name = f"{job_config.experiment_name}-job{job_index}"
        
        # Handle input_files and data_inputs same way as working version
        input_files = job_config.input_files if job_config.input_files else None
        data_inputs = job_config.data_inputs if job_config.data_inputs else None
        
        result_dict = create_and_launch_experiment(
            access_token=access_token,
            experiment_name=unique_experiment_name,
            project_name=job_config.project_name,
            application_name=job_config.application_name,
            computation_resource_name=job_config.computation_resource_name,
            queue_name=job_config.queue_name,
            node_count=job_config.node_count,
            cpu_count=job_config.cpu_count,
            walltime=job_config.walltime,
            group_name=job_config.group_name,
            input_storage_host=job_config.input_storage_host,
            output_storage_host=job_config.output_storage_host,
            input_files=input_files,
            data_inputs=data_inputs,
            gateway_id=job_config.gateway_id,
            auto_schedule=job_config.auto_schedule,
            monitor=False,
        )
        
        operator = AiravataOperator(access_token=access_token)
        experiment_id = result_dict['experiment_id']
        
        # Check status immediately after submission to catch early failures
        try:
            initial_status = operator.get_experiment_status(experiment_id)
            status_value, status_name, status_enum = get_experiment_state_value(initial_status)
            
            # Check if already in terminal state
            if status_value is not None and status_value in [
                ExperimentState.COMPLETED.value,
                ExperimentState.CANCELED.value,
                ExperimentState.FAILED.value
            ]:
                # Already in terminal state
                final_status = status_enum
            else:
                # Monitor until completion
                final_status = monitor_experiment_silent(operator, experiment_id)
        except Exception as e:
            # If we can't check status, log and assume failed
            logger = logging.getLogger(__name__)
            logger.error(f"Error monitoring experiment {experiment_id}: {e}")
            import traceback
            logger.debug(traceback.format_exc())
            final_status = ExperimentState.FAILED
        
        result = ExperimentLaunchResult(**result_dict)
        
        return JobResult(
            job_index=job_index,
            experiment_id=result.experiment_id,
            status=final_status.name,
            result=result,
            success=final_status == ExperimentState.COMPLETED,
        )
    except Exception as e:
        # Log the error for debugging
        import traceback
        error_msg = f"{str(e)}\n{traceback.format_exc()}"
        logger = logging.getLogger(__name__)
        logger.error(f"Job {job_index} failed: {error_msg}")
        
        return JobResult(
            job_index=job_index,
            experiment_id=None,
            status='ERROR',
            result=None,
            success=False,
            error=str(e),
        )


def batch_submit_jobs(
    job_config: JobConfig | Dict[str, Any],
    num_copies: int = 10,
    max_concurrent: int = 5,
    access_token: str | None = None,
) -> List[JobResult]:
    """Submit multiple job copies in batches with progress bar."""
    if access_token is None:
        from airavata_auth.device_auth import AuthContext
        access_token = AuthContext.get_access_token()
    
    console = Console()
    results = []
    log_buffer = deque(maxlen=50)  # Keep last 50 log lines for display
    
    # Custom handler to capture logs to buffer
    class ListHandler(logging.Handler):
        def __init__(self, buffer):
            super().__init__()
            self.buffer = buffer
        
        def emit(self, record):
            msg = self.format(record)
            self.buffer.append(msg)
    
    log_handler = ListHandler(log_buffer)
    log_handler.setLevel(logging.INFO)
    log_handler.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))
    
    # Add to root logger and module logger
    logging.root.addHandler(log_handler)
    logger = logging.getLogger('create_launch_experiment_with_storage')
    logger.addHandler(log_handler)
    
    # Configure progress bar
    progress = Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        TextColumn("•"),
        TextColumn("{task.completed}/{task.total}"),
        TimeElapsedColumn(),
        console=console,
    )
    
    task = progress.add_task(
        f"{num_copies} total, 0 running, 0 completed, 0 failed",
        total=num_copies
    )
    
    # Create layout with logs above and progress below
    layout = Layout()
    layout.split_column(
        Layout(name="logs", size=None),
        Layout(progress, name="progress", size=3)
    )
    
    def make_display():
        # Get logs from buffer - always show the latest logs (they're added to end of deque)
        log_lines = list(log_buffer) if log_buffer else ["No logs yet..."]
        # Show last 20 lines to keep display manageable and scrolled to bottom
        display_lines = log_lines[-20:] if len(log_lines) > 20 else log_lines
        log_text = '\n'.join(display_lines)
        log_panel = Panel(
            log_text, 
            title="Logs (latest)", 
            border_style="blue", 
            height=None,
            expand=False
        )
        layout["logs"].update(log_panel)
        return layout
    
    try:
        # Use Live to keep layout fixed, progress at bottom
        with Live(make_display(), console=console, refresh_per_second=4, screen=True) as live:
            with ThreadPoolExecutor(max_workers=max_concurrent) as executor:
                active_futures = {}
                next_job_index = 0
                
                # Submit initial batch
                while next_job_index < min(max_concurrent, num_copies):
                    future = executor.submit(submit_and_monitor_job, next_job_index, job_config, access_token)
                    active_futures[future] = next_job_index
                    next_job_index += 1
                
                # Process completed jobs and submit new ones
                # Continue until all jobs are submitted AND all active futures are done
                while active_futures or next_job_index < num_copies:
                    completed_futures = [f for f in active_futures if f.done()]
                    
                    for future in completed_futures:
                        job_idx = active_futures.pop(future)
                        
                        try:
                            result = future.result()
                            results.append(result)
                        except Exception as e:
                            # Handle unexpected exceptions
                            results.append(JobResult(
                                job_index=job_idx,
                                experiment_id=None,
                                status='ERROR',
                                result=None,
                                success=False,
                                error=str(e),
                            ))
                    
                    # Submit next jobs if available and we have capacity
                    while next_job_index < num_copies and len(active_futures) < max_concurrent:
                        try:
                            new_future = executor.submit(submit_and_monitor_job, next_job_index, job_config, access_token)
                            active_futures[new_future] = next_job_index
                            next_job_index += 1
                        except Exception as e:
                            # If submission itself fails, mark as error and continue
                            results.append(JobResult(
                                job_index=next_job_index,
                                experiment_id=None,
                                status='ERROR',
                                result=None,
                                success=False,
                                error=f"Submission failed: {str(e)}",
                            ))
                            next_job_index += 1
                    
                    # Update progress bar with counts
                    completed_count = len(results)
                    running_count = len(active_futures)
                    submitted_count = next_job_index
                    successful_count = sum(1 for r in results if r.success)
                    failed_count = completed_count - successful_count
                    
                    # Show submitted count if not all jobs submitted yet
                    if submitted_count < num_copies:
                        status_desc = f"{num_copies} total, {submitted_count} submitted, {running_count} running, {completed_count} completed, {failed_count} failed"
                    else:
                        status_desc = f"{num_copies} total, {running_count} running, {completed_count} completed, {failed_count} failed"
                    
                    progress.update(
                        task,
                        completed=completed_count,
                        description=status_desc
                    )
                    live.update(make_display())
                    
                    if not completed_futures and next_job_index >= num_copies:
                        # Only sleep if nothing changed
                        time.sleep(1)
    
        # Sort results by job_index
        results.sort(key=lambda x: x.job_index)
        return results
    finally:
        # Clean up log handlers
        logging.root.removeHandler(log_handler)
        if log_handler in logger.handlers:
            logger.removeHandler(log_handler)


def main():
    """Main function that sets up job configuration and runs batch submission."""
    from airavata_auth.device_auth import AuthContext
    
    access_token = AuthContext.get_access_token()
    
    # Job configuration - matching create_launch_experiment_with_storage.py exactly
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
    
    job_config = JobConfig(
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
    )
    
    num_copies = 10
    
    try:
        results = batch_submit_jobs(
            job_config=job_config,
            num_copies=num_copies,
            max_concurrent=5,
            access_token=access_token,
        )
        
        # Print summary
        print("\n" + "="*60)
        print(f"Batch submission complete: {num_copies} jobs")
        print("="*60)
        successful = sum(1 for r in results if r.success)
        print(f"Successful: {successful}/{num_copies}")
        print(f"Failed: {num_copies - successful}/{num_copies}")
        print("\nJob Results:")
        for result in results:
            status_symbol = "✓" if result.success else "✗"
            exp_id = result.experiment_id or 'N/A'
            print(f"  {status_symbol} Job {result.job_index}: {result.status} "
                  f"(ID: {exp_id})")
        print("="*60)
        
        return results
        
    except Exception as e:
        print(f"Failed to run batch submission: {repr(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

