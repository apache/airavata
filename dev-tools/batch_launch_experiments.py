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
from io import StringIO

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


def monitor_experiment_silent(operator: AiravataOperator, experiment_id: str, check_interval: int = 30) -> ExperimentState:
    """Monitor experiment silently until completion. Returns final status."""
    while True:
        status = operator.get_experiment_status(experiment_id)
        
        if status in [
            ExperimentState.COMPLETED,
            ExperimentState.CANCELED,
            ExperimentState.FAILED,
        ]:
            return status
        
        time.sleep(check_interval)


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
        result_dict = create_and_launch_experiment(
            access_token=access_token,
            experiment_name=job_config.experiment_name,
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
            input_files=job_config.input_files,
            data_inputs=job_config.data_inputs,
            gateway_id=job_config.gateway_id,
            auto_schedule=job_config.auto_schedule,
            monitor=False,
        )
        
        operator = AiravataOperator(access_token=access_token)
        final_status = monitor_experiment_silent(operator, result_dict['experiment_id'])
        
        result = ExperimentLaunchResult(**result_dict)
        
        return JobResult(
            job_index=job_index,
            experiment_id=result.experiment_id,
            status=final_status.name,
            result=result,
            success=final_status == ExperimentState.COMPLETED,
        )
    except Exception as e:
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
    log_buffer = StringIO()
    
    # Add handler to capture logs (without reconfiguring)
    log_handler = logging.StreamHandler(log_buffer)
    log_handler.setLevel(logging.INFO)
    logging.root.addHandler(log_handler)
    
    try:
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
            f"Jobs: 0/{num_copies} completed, 0 running",
            total=num_copies
        )
        
        # Create layout with logs above and progress below
        layout = Layout()
        layout.split_column(
            Layout(name="logs", size=None),
            Layout(progress, name="progress", size=3)
        )
        
        def make_display():
            log_content = log_buffer.getvalue()
            log_lines = log_content.split('\n')[-20:]  # Last 20 lines
            log_panel = Panel('\n'.join(log_lines), title="Logs", border_style="blue")
            layout["logs"].update(log_panel)
            layout["progress"].update(progress)
            return layout
        
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
                while active_futures:
                    completed_futures = [f for f in active_futures if f.done()]
                    
                    for future in completed_futures:
                        results.append(future.result())
                        active_futures.pop(future)
                        
                        # Submit next job if available
                        if next_job_index < num_copies:
                            new_future = executor.submit(submit_and_monitor_job, next_job_index, job_config, access_token)
                            active_futures[new_future] = next_job_index
                            next_job_index += 1
                    
                    # Update progress bar and refresh display
                    completed_count = len(results)
                    running_count = len(active_futures)
                    progress.update(
                        task,
                        completed=completed_count,
                        description=f"Jobs: {completed_count}/{num_copies} completed, {running_count} running"
                    )
                    live.update(make_display())
                    
                    if not completed_futures:
                        time.sleep(1)
        
        # Sort results by job_index
        results.sort(key=lambda x: x.job_index)
        return results
    finally:
        # Clean up log handler
        logging.root.removeHandler(log_handler)


def main():
    """Main function that sets up job configuration and runs batch submission."""
    from airavata_auth.device_auth import AuthContext
    
    access_token = AuthContext.get_access_token()
    
    # Job configuration
    job_config = JobConfig(
        experiment_name='Test',
        project_name='Default Project',
        application_name='NAMD-test',
        computation_resource_name='NeuroData25VC2',
        queue_name='cloud',
        node_count=1,
        cpu_count=1,
        walltime=5,
        group_name='Default',
        input_storage_host='gateway.dev.cybershuttle.org',
        output_storage_host='149.165.169.12',
        input_files=None,
        data_inputs=None,
        gateway_id=None,
        auto_schedule=False,
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

