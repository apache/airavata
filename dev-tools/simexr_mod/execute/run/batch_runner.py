import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Dict, Any

import pandas as pd
from tqdm import tqdm

from execute.base import BaseRunner
from execute.logging.run_logger import RunLogger
from execute.run.simulation_runner import SimulationRunner
from execute.utils.requirements_manager import RequirementManager
from db import get_simulation_path, store_simulation_results



@dataclass
class BatchRunner(BaseRunner):
    """
    Batch executor: ensures dependencies, adjusts sys.path, runs grid,
    writes CSV, and persists to DB.
    """
    reqs: RequirementManager = field(default_factory=RequirementManager)
    single_runner: SimulationRunner = field(default_factory=SimulationRunner)

    def run(
        self,
        model_id: str,
        param_grid: List[Dict[str, Any]],
        output_csv: str = "results.csv",
        db_path: str = "mcp.db",
    ) -> None:
        script_path = Path(get_simulation_path(model_id, db_path=db_path))
        model_dir = script_path.parent
        lib_dir = model_dir / "lib"

        logger = RunLogger.get_logger(script_path)
        logger.info(f"[BATCH] start | model_id={model_id} | script={script_path}")
        logger.info(f"[BATCH] grid_size={len(param_grid)} | output_csv={output_csv} | db={db_path}")

        # Install deps once (best-effort)
        try:
            reqs = self.reqs.extract(script_path.read_text())
            logger.info(f"[BATCH] requirements: {reqs or 'none'}")
            self.reqs.install(reqs, lib_dir)
            logger.info("[BATCH] requirements ready")
        except Exception as e:
            logger.exception(f"[BATCH] dependency setup failed: {e}")
            # continue; simulate() may still work if deps are already in env

        # Import path setup
        if str(lib_dir) not in sys.path:
            sys.path.insert(0, str(lib_dir))
        if str(model_dir) not in sys.path:
            sys.path.insert(0, str(model_dir))

        rows: List[Dict[str, Any]] = []
        for i, p in enumerate(tqdm(param_grid, desc=f"Running {model_id}"), start=1):
            logger.info(f"[BATCH] run {i}/{len(param_grid)}")
            row = self.single_runner.run(script_path, p)
            rows.append(row)
            if not row.get("_ok", False):
                logger.warning(f"[BATCH] run {i} failed: {row.get('_error_type')} | {row.get('_error_msg')}")

        # Persist CSV
        try:
            df = pd.DataFrame(rows)
            out = Path(output_csv)
            out.parent.mkdir(parents=True, exist_ok=True)
            df.to_csv(out, index=False)
            logger.info(f"[BATCH] wrote CSV | rows={len(df)} | path={out}")
            print(f"{len(df)} rows → {out}")  # retain original print
        except Exception as e:
            logger.exception(f"[BATCH] failed to write CSV: {e}")

        # Persist to DB (best-effort)
        try:
            store_simulation_results(
                model_id=model_id,
                rows=rows,
                param_keys=list(param_grid[0].keys()) if param_grid else [],
                db_path=db_path,
            )
            logger.info(f"[BATCH] stored {len(rows)} rows in DB {db_path}")
            print(f"Stored {len(rows)} rows in DB {db_path}")  # retain original print
        except Exception as e:
            logger.exception(f"[BATCH] DB persistence failed: {e}")

        logger.info("[BATCH] done")