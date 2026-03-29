import datetime
import io
import traceback
import logging
from contextlib import redirect_stdout, redirect_stderr
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Any

from execute.base import BaseRunner
from execute.loader.simulate_loader import SimulateLoader
from execute.logging.run_logger import RunLogger
from execute.utils.json_utlils import json_convert


@dataclass
class SimulationRunner(BaseRunner):
    """
    Execute simulate(**params) capturing stdout/stderr and logging in detail.
    Returns a row that merges params + results + diagnostic fields.
    """
    loader: SimulateLoader = field(default_factory=SimulateLoader)
    logger_factory: RunLogger = field(default_factory=RunLogger)
    
    def __post_init__(self):
        """Initialize logging."""
        self.logger = logging.getLogger("SimulationRunner")
        self.logger.setLevel(logging.INFO)

    def run(self, script_path: Path, params: Dict[str, Any]) -> Dict[str, Any]:
        """Run a single simulation with the given parameters."""
        self.logger.info(f"[SIMULATION_RUNNER] Starting simulation execution")
        self.logger.info(f"[SIMULATION_RUNNER] Script path: {script_path}")
        self.logger.info(f"[SIMULATION_RUNNER] Parameters: {params}")
        
        logger = self.logger_factory.get_logger(script_path)
        run_id = self._generate_run_id()

        logger.info(f"[RUN] start simulate | script={script_path.name} | run_id={run_id}")
        logger.debug(f"[RUN] params={params}")

        start_ts = datetime.datetime.utcnow()
        cap_out, cap_err = io.StringIO(), io.StringIO()

        try:
            self.logger.info(f"[SIMULATION_RUNNER] Executing simulation...")
            result = self._execute_simulation(script_path, params, cap_out, cap_err)
            duration = (datetime.datetime.utcnow() - start_ts).total_seconds()
            
            self.logger.info(f"[SIMULATION_RUNNER] Simulation completed successfully in {duration:.3f}s")
            self.logger.info(f"[SIMULATION_RUNNER] Result keys: {list(result.keys())}")
            
            # Log preview of results (first 5 rows for time series data)
            self._log_result_preview(result)
            
            row = self._create_success_row(params, result, duration, cap_out, cap_err, run_id, script_path)
            self._log_success(logger, duration, result, row)
            
        except Exception as e:
            duration = (datetime.datetime.utcnow() - start_ts).total_seconds()
            self.logger.error(f"[SIMULATION_RUNNER] Simulation failed after {duration:.3f}s: {str(e)}")
            self.logger.error(f"[SIMULATION_RUNNER] Error type: {type(e).__name__}")
            
            row = self._create_error_row(params, e, duration, cap_out, cap_err, run_id, script_path)
            self._log_error(logger, e, duration)

        self.logger.info(f"[SIMULATION_RUNNER] Appending results to log file")
        self.logger_factory.append_jsonl(script_path, row)
        self.logger.info(f"[SIMULATION_RUNNER] Simulation execution completed")
        return row
    
    def _generate_run_id(self) -> str:
        """Generate a unique run ID."""
        return datetime.datetime.utcnow().isoformat(timespec="seconds") + "Z"
    
    def _execute_simulation(self, script_path: Path, params: Dict[str, Any], 
                          cap_out: io.StringIO, cap_err: io.StringIO) -> Dict[str, Any]:
        """Execute the simulation and return results."""
        self.logger.info(f"[SIMULATION_RUNNER] Importing simulate function from {script_path}")
        simulate = self.loader.import_simulate(script_path)
        self.logger.info(f"[SIMULATION_RUNNER] Successfully imported simulate function")
        
        self.logger.info(f"[SIMULATION_RUNNER] Calling simulate(**params) with captured stdout/stderr")
        with redirect_stdout(cap_out), redirect_stderr(cap_err):
            res = simulate(**params)
        self.logger.info(f"[SIMULATION_RUNNER] simulate() function completed")

        if not isinstance(res, dict):
            self.logger.error(f"[SIMULATION_RUNNER] simulate() returned {type(res)}, expected dict")
            raise TypeError(f"simulate() must return dict, got {type(res)}")
        
        self.logger.info(f"[SIMULATION_RUNNER] Result validation passed, returning {len(res)} keys")
        return res
    
    def _create_success_row(self, params: Dict[str, Any], result: Dict[str, Any], 
                           duration: float, cap_out: io.StringIO, cap_err: io.StringIO,
                           run_id: str, script_path: Path) -> Dict[str, Any]:
        """Create a success result row."""
        row = {
            **params,
            **result,
            "_ok": True,
            "_duration_s": duration,
            "_stdout": cap_out.getvalue(),
            "_stderr": cap_err.getvalue(),
            "_error_type": "",
            "_error_msg": "",
            "_traceback": "",
            "_run_id": run_id,
            "_script": str(script_path),
        }
        return json_convert(row)
    
    def _create_error_row(self, params: Dict[str, Any], error: Exception, 
                         duration: float, cap_out: io.StringIO, cap_err: io.StringIO,
                         run_id: str, script_path: Path) -> Dict[str, Any]:
        """Create an error result row."""
        return {
            **params,
            "_ok": False,
            "_duration_s": duration,
            "_stdout": cap_out.getvalue(),
            "_stderr": cap_err.getvalue(),
            "_error_type": type(error).__name__,
            "_error_msg": str(error),
            "_traceback": traceback.format_exc(),
            "_run_id": run_id,
            "_script": str(script_path),
        }
    
    def _log_success(self, logger, duration: float, result: Dict[str, Any], row: Dict[str, Any]) -> None:
        """Log successful execution."""
        logger.info(f"[RUN] ok | duration={duration:.3f}s | keys={list(result.keys())[:8]}")
        if row["_stderr"]:
            logger.warning(f"[RUN] stderr non-empty ({len(row['_stderr'])} chars)")
    
    def _log_result_preview(self, result: Dict[str, Any]) -> None:
        """Log a preview of the simulation results (first 5 rows)."""
        self.logger.info(f"[SIMULATION_RUNNER] === RESULT PREVIEW (First 5 rows) ===")
        
        # Show time series data if available
        if 't' in result and isinstance(result['t'], (list, tuple)) and len(result['t']) > 0:
            t_data = result['t'][:5]
            self.logger.info(f"[SIMULATION_RUNNER] Time (t): {t_data}")
        
        if 'x' in result and isinstance(result['x'], (list, tuple)) and len(result['x']) > 0:
            x_data = result['x'][:5]
            self.logger.info(f"[SIMULATION_RUNNER] X trajectory: {x_data}")
        
        if 'y' in result and isinstance(result['y'], (list, tuple)) and len(result['y']) > 0:
            y_data = result['y'][:5]
            self.logger.info(f"[SIMULATION_RUNNER] Y trajectory: {y_data}")
        
        # Show key scalar results
        scalar_keys = ['success', 'mu', 'z0', 'eval_time', 't_iteration', 'grid_points', 'mgrid_size']
        for key in scalar_keys:
            if key in result:
                self.logger.info(f"[SIMULATION_RUNNER] {key}: {result[key]}")
        
        # Show solver message if available
        if 'solver_message' in result:
            self.logger.info(f"[SIMULATION_RUNNER] Solver message: {result['solver_message']}")
        
        self.logger.info(f"[SIMULATION_RUNNER] === END RESULT PREVIEW ===")
    
    def _log_error(self, logger, error: Exception, duration: float) -> None:
        """Log error execution."""
        logger.error(f"[RUN] fail | duration={duration:.3f}s | {type(error).__name__}: {error}")
        if isinstance(error, (TypeError, ValueError)):
            logger.error("[RUN] hint: check integer-only sizes (e.g., N, array shapes) and dtype coercion.")