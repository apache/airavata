from __future__ import annotations
from pathlib import Path
from typing import Any, Dict, List, Optional
import json

from db.config.database import DatabaseConfig
from db import get_simulation_path, store_simulation_results
from reasoning.base import BaseSimulationTool

# Import run_simulation function
from execute.run.simulation_runner import SimulationRunner
from execute.run.batch_runner import BatchRunner


class SimulateTools(BaseSimulationTool):
    """
    Orchestrates simulation runs for a given model_id and persists results.

    - Keeps a default model_id (optional).
    - Resolves script paths through db.store.get_simulation_path.
    - Executes via your existing run_simulation(script_path, params).
    - Appends rows to DB via store_simulation_results.
    - Provides JSON tool adapters for agent/function-calling integrations.
    """

    def __init__(self, db_config: DatabaseConfig, default_model_id: Optional[str] = None):
        super().__init__(db_path=db_config.database_path, default_model_id=default_model_id)
        self.db_config = db_config

    # ------------------------ core helpers ------------------------

    def resolve_script_path(self, model_id: Optional[str] = None) -> Path:
        mid = model_id or self.default_model_id
        if not mid:
            raise ValueError("model_id is required (no default_model_id set).")
        return Path(get_simulation_path(mid, db_path=self.db_path))

    def run_simulation_for_model(self, params: Dict[str, Any], model_id: Optional[str] = None) -> Dict[str, Any]:
        """
        Resolve script for model_id, run simulate(**params), store a single row, and return it.
        """
        mid = model_id or self.default_model_id
        if not mid:
            raise ValueError("model_id is required (no default_model_id set).")

        script_path = self.resolve_script_path(mid)
        runner = SimulationRunner()
        row = runner.run(script_path, params)

        # Persist (append) this row to the model’s results
        param_keys = list(params.keys())
        store_simulation_results(model_id=mid, rows=[row], param_keys=param_keys, db_path=self.db_path)
        return row

    def run_batch_for_model(self, grid: List[Dict[str, Any]], model_id: Optional[str] = None) -> List[Dict[str, Any]]:
        """
        Run multiple param combinations for model_id and append all results to DB.
        """
        mid = model_id or self.default_model_id
        if not mid:
            raise ValueError("model_id is required (no default_model_id set).")

        script_path = self.resolve_script_path(mid)
        runner = SimulationRunner()
        rows: List[Dict[str, Any]] = [runner.run(script_path, p) for p in grid]

        param_keys = list(grid[0].keys()) if grid and isinstance(grid[0], dict) else []
        if rows:
            store_simulation_results(model_id=mid, rows=rows, param_keys=param_keys, db_path=self.db_path)
        return rows

    # ------------------------ optional utilities ------------------------

    @staticmethod
    def cartesian_grid(param_to_values: Dict[str, List[Any]]) -> List[Dict[str, Any]]:
        """
        Build a cartesian product grid: {'a':[1,2], 'b':[10]} -> [{'a':1,'b':10},{'a':2,'b':10}]
        """
        from itertools import product
        keys = list(param_to_values.keys())
        vals = [param_to_values[k] for k in keys]
        return [dict(zip(keys, combo)) for combo in product(*vals)]

    # ------------------------ tool adapters (for agents) ------------------------

    def tool_run_simulation(self, payload_json: str) -> str:
        """
        JSON adapter for agents / function-calling.

        Input JSON:
          {
            "model_id": "my_model_abc123",   // optional if default_model_id set
            "params": { "N": 20, "dt": 0.1 },
            "db_path": "mcp.db"              // optional: overrides ctor setting
          }

        Returns JSON: the stored row (includes _ok/_error fields, stdout/stderr, etc.).
        """
        payload = json.loads(payload_json)
        model_id = payload.get("model_id") or self.default_model_id
        if not model_id:
            raise ValueError("tool_run_simulation: 'model_id' is required (no default_model_id set).")
        if "db_path" in payload and payload["db_path"] != self.db_path:
            self.db_path = payload["db_path"]

        params = payload.get("params", {})
        row = self.run_simulation_for_model(params=params, model_id=model_id)
        return json.dumps(row)

    def tool_run_batch(self, payload_json: str) -> str:
        """
        JSON adapter for agents / function-calling.

        Input JSON:
          {
            "model_id": "my_model_abc123", // optional if default_model_id set
            "grid": [ {"N": 10}, {"N": 20} ],
            "db_path": "mcp.db"            // optional: overrides ctor setting
          }

        Returns JSON: list of stored rows.
        """
        payload = json.loads(payload_json)
        model_id = payload.get("model_id") or self.default_model_id
        if not model_id:
            raise ValueError("tool_run_batch: 'model_id' is required (no default_model_id set).")
        if "db_path" in payload and payload["db_path"] != self.db_path:
            self.db_path = payload["db_path"]

        grid = payload.get("grid", [])
        rows = self.run_batch_for_model(grid=grid, model_id=model_id)
        return json.dumps(rows)
