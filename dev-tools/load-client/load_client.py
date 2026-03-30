#!/usr/bin/env python3
"""
Airavata Load Client — unified CLI for experiment launching and load testing.

Modes:
  single  — create and launch one experiment
  batch   — launch N copies of a scenario from config
  load    — run all scenarios from YAML config (concurrent users, metrics)
"""

import argparse
import csv
import logging
import os
import sys
import time
from collections import deque
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Union

import yaml
from pydantic import BaseModel
from rich.console import Console
from rich.layout import Layout
from rich.live import Live
from rich.panel import Panel
from rich.progress import BarColumn, Progress, SpinnerColumn, TextColumn, TimeElapsedColumn

os.environ.setdefault('AUTH_SERVER_URL', "https://auth.dev.cybershuttle.org")
os.environ.setdefault('API_SERVER_HOSTNAME', "api.dev.cybershuttle.org")
os.environ.setdefault('GATEWAY_URL', "https://gateway.dev.cybershuttle.org")
os.environ.setdefault('STORAGE_RESOURCE_HOST', "gateway.dev.cybershuttle.org")

from airavata_auth.device_auth import AuthContext
from airavata_experiments.airavata import AiravataOperator
from airavata.model.status.ttypes import ExperimentState

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
)
logger = logging.getLogger(__name__)
console = Console()


# ---------------------------------------------------------------------------
# Pydantic models
# ---------------------------------------------------------------------------

class ScenarioConfig(BaseModel):
    name: str
    experiment_name: str
    project_name: str
    application_name: str
    computation_resource_name: str
    queue_name: str
    node_count: int
    cpu_count: int
    walltime: int
    group_name: str = "Default"
    input_storage_host: Optional[str] = None
    output_storage_host: Optional[str] = None
    input_files: Dict[str, Union[str, List[str]]] = {}
    data_inputs: Dict[str, Union[str, int, float]] = {}
    gateway_id: Optional[str] = None
    auto_schedule: bool = False
    # load-mode fields
    concurrent_users: int = 1
    iterations_per_user: int = 1
    delay_between_submissions_ms: int = 0


class LoadConfig(BaseModel):
    scenarios: List[ScenarioConfig]


# ---------------------------------------------------------------------------
# Job result dataclass
# ---------------------------------------------------------------------------

@dataclass
class JobResult:
    job_index: int
    scenario_name: str
    experiment_id: Optional[str]
    status: str
    success: bool
    submit_time: float = 0.0
    finish_time: float = 0.0
    error: Optional[str] = None

    @property
    def elapsed_seconds(self) -> float:
        if self.finish_time and self.submit_time:
            return self.finish_time - self.submit_time
        return 0.0


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _get_state_value(status) -> tuple:
    """Return (int_value, name_str, ExperimentState) from any status object."""
    if isinstance(status, ExperimentState):
        return status.value, status.name, status
    if hasattr(status, 'state'):
        state = status.state
        if isinstance(state, ExperimentState):
            return state.value, state.name, state
        if hasattr(state, 'value'):
            try:
                enum = ExperimentState(state.value)
                return state.value, state.name if hasattr(state, 'name') else str(state), enum
            except (ValueError, TypeError):
                pass
    val = status.value if hasattr(status, 'value') else None
    name = status.name if hasattr(status, 'name') else str(status)
    if val is not None:
        try:
            return val, name, ExperimentState(val)
        except (ValueError, TypeError):
            pass
    return None, name, ExperimentState.FAILED


_TERMINAL = frozenset([
    ExperimentState.COMPLETED.value,
    ExperimentState.CANCELED.value,
    ExperimentState.FAILED.value,
])


def prepare_inputs(
    input_files: Dict[str, Union[str, List[str]]],
    data_inputs: Dict[str, Union[str, int, float]],
) -> dict:
    """Build the experiment_inputs dict expected by AiravataOperator.launch_experiment."""
    inputs: dict = {}
    for name, paths in (input_files or {}).items():
        if isinstance(paths, list):
            inputs[name] = {"type": "uri[]", "value": [str(Path(p).resolve()) for p in paths]}
        else:
            inputs[name] = {"type": "uri", "value": str(Path(paths).resolve())}
    for name, value in (data_inputs or {}).items():
        if isinstance(value, int):
            inputs[name] = {"type": "int", "value": value}
        elif isinstance(value, float):
            inputs[name] = {"type": "float", "value": value}
        else:
            inputs[name] = {"type": "string", "value": str(value)}
    if not inputs:
        inputs = {"__no_inputs__": {"type": "string", "value": ""}}
    return inputs


def monitor_experiment(operator: AiravataOperator, experiment_id: str, check_interval: int = 30) -> ExperimentState:
    """Poll until the experiment reaches a terminal state. Returns final ExperimentState."""
    max_checks = 3600
    count = 0
    initial_interval = min(check_interval, 5)
    while count < max_checks:
        try:
            status = operator.get_experiment_status(experiment_id)
            val, name, enum = _get_state_value(status)
            is_terminal = (val in _TERMINAL) if val is not None else (name in {'COMPLETED', 'CANCELED', 'FAILED'})
            if is_terminal:
                return enum
        except Exception as exc:
            logger.warning("Status check error for %s (attempt %d): %s", experiment_id, count, exc)
            if count > 10:
                logger.error("Too many failures monitoring %s; assuming FAILED", experiment_id)
                return ExperimentState.FAILED
        sleep = initial_interval if count < 6 else check_interval
        time.sleep(sleep)
        count += 1
    logger.error("Monitoring timeout for %s; assuming FAILED", experiment_id)
    return ExperimentState.FAILED


# ---------------------------------------------------------------------------
# Core launch function
# ---------------------------------------------------------------------------

def launch_experiment(
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
    input_storage_host: Optional[str] = None,
    output_storage_host: Optional[str] = None,
    input_files: Optional[Dict] = None,
    data_inputs: Optional[Dict] = None,
    gateway_id: Optional[str] = None,
    auto_schedule: bool = False,
    do_monitor: bool = True,
) -> dict:
    """Create and launch one experiment; return a result dict."""
    operator = AiravataOperator(access_token=access_token)
    inputs = prepare_inputs(input_files or {}, data_inputs or {})

    launch_state = operator.launch_experiment(
        experiment_name=experiment_name,
        project=project_name,
        app_name=application_name,
        inputs=inputs,
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

    result = {
        "experiment_id": launch_state.experiment_id,
        "process_id": launch_state.process_id,
        "experiment_dir": launch_state.experiment_dir,
        "storage_host": launch_state.sr_host,
        "mount_point": str(launch_state.mount_point),
    }

    if do_monitor:
        monitor_experiment(operator, launch_state.experiment_id)

    return result


# ---------------------------------------------------------------------------
# Worker used by batch / load modes
# ---------------------------------------------------------------------------

def _run_job(
    job_index: int,
    scenario: ScenarioConfig,
    access_token: str,
    name_suffix: str = "",
) -> JobResult:
    submit_time = time.time()
    exp_name = f"{scenario.experiment_name}{name_suffix}"
    try:
        result = launch_experiment(
            access_token=access_token,
            experiment_name=exp_name,
            project_name=scenario.project_name,
            application_name=scenario.application_name,
            computation_resource_name=scenario.computation_resource_name,
            queue_name=scenario.queue_name,
            node_count=scenario.node_count,
            cpu_count=scenario.cpu_count,
            walltime=scenario.walltime,
            group_name=scenario.group_name,
            input_storage_host=scenario.input_storage_host,
            output_storage_host=scenario.output_storage_host,
            input_files=scenario.input_files or None,
            data_inputs=scenario.data_inputs or None,
            gateway_id=scenario.gateway_id,
            auto_schedule=scenario.auto_schedule,
            do_monitor=True,
        )
        # Determine final state
        operator = AiravataOperator(access_token=access_token)
        status = operator.get_experiment_status(result["experiment_id"])
        val, name, _ = _get_state_value(status)
        success = val == ExperimentState.COMPLETED.value
        return JobResult(
            job_index=job_index,
            scenario_name=scenario.name,
            experiment_id=result["experiment_id"],
            status=name,
            success=success,
            submit_time=submit_time,
            finish_time=time.time(),
        )
    except Exception as exc:
        import traceback
        return JobResult(
            job_index=job_index,
            scenario_name=scenario.name,
            experiment_id=None,
            status="ERROR",
            success=False,
            submit_time=submit_time,
            finish_time=time.time(),
            error=f"{exc}\n{traceback.format_exc()}",
        )


def _run_with_progress(
    jobs: List[tuple],  # list of (job_index, scenario, suffix)
    access_token: str,
    max_workers: int,
    description: str,
) -> List[JobResult]:
    """Submit jobs with a Rich progress bar. Returns results sorted by job_index."""
    results: List[JobResult] = []
    log_buf: deque = deque(maxlen=50)

    class _BufHandler(logging.Handler):
        def emit(self, record: logging.LogRecord) -> None:
            log_buf.append(self.format(record))

    handler = _BufHandler()
    handler.setFormatter(logging.Formatter('%(asctime)s %(levelname)s %(message)s'))
    logging.root.addHandler(handler)

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
    task_id = progress.add_task(description, total=len(jobs))
    layout = Layout()
    layout.split_column(Layout(name="logs"), Layout(progress, name="progress", size=3))

    def _refresh():
        lines = list(log_buf)[-20:] or ["No logs yet..."]
        layout["logs"].update(Panel("\n".join(lines), title="Logs (latest)", border_style="blue"))
        return layout

    try:
        with Live(_refresh(), console=console, refresh_per_second=4, screen=True) as live:
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                pending: dict = {}
                queue = list(jobs)
                # seed initial batch
                while queue and len(pending) < max_workers:
                    idx, scen, suffix = queue.pop(0)
                    f = executor.submit(_run_job, idx, scen, access_token, suffix)
                    pending[f] = idx

                while pending or queue:
                    done = [f for f in pending if f.done()]
                    for f in done:
                        pending.pop(f)
                        try:
                            results.append(f.result())
                        except Exception as exc:
                            results.append(JobResult(
                                job_index=-1, scenario_name="?",
                                experiment_id=None, status="ERROR",
                                success=False, error=str(exc),
                            ))
                        progress.update(task_id, advance=1)

                    while queue and len(pending) < max_workers:
                        idx, scen, suffix = queue.pop(0)
                        f = executor.submit(_run_job, idx, scen, access_token, suffix)
                        pending[f] = idx

                    live.update(_refresh())
                    if not done and pending:
                        time.sleep(1)
    finally:
        logging.root.removeHandler(handler)

    results.sort(key=lambda r: r.job_index)
    return results


def _write_csv(results: List[JobResult], path: str) -> None:
    with open(path, "w", newline="") as fh:
        w = csv.writer(fh)
        w.writerow(["job_index", "scenario_name", "experiment_id", "status", "success", "elapsed_seconds", "error"])
        for r in results:
            w.writerow([r.job_index, r.scenario_name, r.experiment_id, r.status,
                        r.success, f"{r.elapsed_seconds:.2f}", r.error or ""])
    console.print(f"[green]Metrics written to {path}[/green]")


def _print_summary(results: List[JobResult], label: str) -> None:
    total = len(results)
    ok = sum(1 for r in results if r.success)
    console.print(f"\n{'='*60}")
    console.print(f"{label}: {total} jobs")
    console.print(f"  Successful : {ok}/{total}")
    console.print(f"  Failed     : {total - ok}/{total}")
    for r in results:
        sym = "[green]✓[/green]" if r.success else "[red]✗[/red]"
        console.print(f"  {sym} [{r.job_index}] {r.scenario_name} — {r.status}"
                      + (f" ({r.experiment_id})" if r.experiment_id else ""))
    console.print('='*60)


# ---------------------------------------------------------------------------
# CLI sub-commands
# ---------------------------------------------------------------------------

def cmd_single(args: argparse.Namespace) -> None:
    access_token = AuthContext.get_access_token()
    result = launch_experiment(
        access_token=access_token,
        experiment_name=args.experiment_name,
        project_name=args.project,
        application_name=args.application,
        computation_resource_name=args.resource,
        queue_name=args.queue,
        node_count=args.node_count,
        cpu_count=args.cpu_count,
        walltime=args.walltime,
        group_name=args.group,
        input_storage_host=args.input_storage_host,
        output_storage_host=args.output_storage_host,
        do_monitor=not args.no_monitor,
    )
    console.print("\n[bold green]Experiment launched successfully![/bold green]")
    for k, v in result.items():
        console.print(f"  {k}: {v}")


def cmd_batch(args: argparse.Namespace) -> None:
    with open(args.config) as fh:
        raw = yaml.safe_load(fh)
    cfg = LoadConfig(**raw)

    scenario_map = {s.name: s for s in cfg.scenarios}
    if args.scenario not in scenario_map:
        console.print(f"[red]Scenario '{args.scenario}' not found in config.[/red]")
        sys.exit(1)
    scenario = scenario_map[args.scenario]

    access_token = AuthContext.get_access_token()
    n = args.copies
    jobs = [(i, scenario, f"-job{i}") for i in range(n)]
    results = _run_with_progress(jobs, access_token, max_workers=args.max_concurrent,
                                 description=f"Batch: {scenario.name}")
    _print_summary(results, f"Batch '{scenario.name}'")
    if args.csv:
        _write_csv(results, args.csv)


def cmd_load(args: argparse.Namespace) -> None:
    with open(args.config) as fh:
        raw = yaml.safe_load(fh)
    cfg = LoadConfig(**raw)

    access_token = AuthContext.get_access_token()
    all_jobs: List[tuple] = []
    job_index = 0
    for scenario in cfg.scenarios:
        total = scenario.concurrent_users * scenario.iterations_per_user
        for i in range(total):
            all_jobs.append((job_index, scenario, f"-u{i // scenario.iterations_per_user}-iter{i % scenario.iterations_per_user}"))
            job_index += 1
            if scenario.delay_between_submissions_ms > 0 and i < total - 1:
                time.sleep(scenario.delay_between_submissions_ms / 1000.0)

    max_workers = max(s.concurrent_users for s in cfg.scenarios)
    results = _run_with_progress(all_jobs, access_token, max_workers=max_workers,
                                 description="Load test")
    _print_summary(results, "Load test")
    if args.csv:
        _write_csv(results, args.csv)


# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------

def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="load_client.py",
        description="Airavata experiment launcher and load tester",
    )
    sub = parser.add_subparsers(dest="command", required=True)

    # single
    p_single = sub.add_parser("single", help="Create and launch one experiment")
    p_single.add_argument("--experiment-name", required=True)
    p_single.add_argument("--project", required=True)
    p_single.add_argument("--application", required=True)
    p_single.add_argument("--resource", required=True)
    p_single.add_argument("--queue", required=True)
    p_single.add_argument("--node-count", type=int, default=1)
    p_single.add_argument("--cpu-count", type=int, default=1)
    p_single.add_argument("--walltime", type=int, default=30)
    p_single.add_argument("--group", default="Default")
    p_single.add_argument("--input-storage-host")
    p_single.add_argument("--output-storage-host")
    p_single.add_argument("--no-monitor", action="store_true",
                          help="Submit without waiting for completion")
    p_single.set_defaults(func=cmd_single)

    # batch
    p_batch = sub.add_parser("batch", help="Launch N copies of a scenario")
    p_batch.add_argument("--config", required=True, help="Path to load-config.yml")
    p_batch.add_argument("--scenario", required=True, help="Scenario name in config")
    p_batch.add_argument("--copies", type=int, default=10)
    p_batch.add_argument("--max-concurrent", type=int, default=5)
    p_batch.add_argument("--csv", metavar="FILE", help="Write metrics CSV to FILE")
    p_batch.set_defaults(func=cmd_batch)

    # load
    p_load = sub.add_parser("load", help="Run all scenarios from config")
    p_load.add_argument("--config", required=True, help="Path to load-config.yml")
    p_load.add_argument("--csv", metavar="FILE", help="Write metrics CSV to FILE")
    p_load.set_defaults(func=cmd_load)

    return parser


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
