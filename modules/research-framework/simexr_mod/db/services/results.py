import datetime
import json
import sqlite3
from pathlib import Path
from typing import Dict, Any, List
import pandas as pd

from ..repositories.simulation import SimulationRepository
from ..utils.json_utils import _safe_parse
from ..utils.transform_utils import _explode_row
from ..base import BaseResultsService

# Import sanitize_metadata with fallback
try:
    from core.script_utils import sanitize_metadata
except ImportError:
    def sanitize_metadata(data, media_dir, media_paths, prefix=""):
        return data


class ResultsService(BaseResultsService):
    def __init__(self, simulation_repo: SimulationRepository):
        super().__init__(repository=simulation_repo)
        self.simulation_repo = simulation_repo

    def load_results(self,
            db_path: str | Path = "mcp.db",
            model_id: str | None = None,
    ) -> pd.DataFrame:
        """
        Load the `results` table, parse JSON cols, and EXPLODE any list/array fields
        (from params or outputs) into rowwise records.

        Returned columns:
          model_id, ts, step, <param fields>, <output fields>

        Notes:
          - If multiple array fields have different lengths in a run, scalars are broadcast
            and shorter arrays are padded with None to match the longest length.
          - If a run has no arrays at all, it's kept as a single row with step=0.
        """
        con = sqlite3.connect(str(db_path))
        try:
            raw_df = pd.read_sql("SELECT model_id, params, outputs, ts FROM results", con)
        finally:
            con.close()

        if model_id is not None:
            raw_df = raw_df[raw_df["model_id"] == model_id].reset_index(drop=True)

        # Parse JSON
        raw_df["params"] = raw_df["params"].apply(_safe_parse)
        raw_df["outputs"] = raw_df["outputs"].apply(_safe_parse)

        # Drop unparseable rows
        raw_df = raw_df[raw_df["params"].apply(bool) & raw_df["outputs"].apply(bool)].reset_index(drop=True)

        # Explode each row based on list-like fields
        exploded_frames: list[pd.DataFrame] = []
        for _, r in raw_df.iterrows():
            exploded = _explode_row(
                model_id=r["model_id"],
                ts=r["ts"],
                params=r["params"],
                outputs=r["outputs"],
            )
            exploded_frames.append(exploded)

        if not exploded_frames:
            return pd.DataFrame(columns=["model_id", "ts", "step"])

        final = pd.concat(exploded_frames, ignore_index=True)

        # Optional: stable column ordering → id/timestamps first, then others (params before outputs if you want)
        # Already merged; if you need specific ordering, you can sort keys or provide a custom order here.

        return final

    def store_simulation_results(
            self,
            model_id: str,
            rows: List[Dict[str, Any]],
            param_keys: List[str] | None = None,
            db_path: str | Path = None,
    ) -> None:
        """
        Persist experiment result rows with sanitized outputs and saved media.

        DB schema:
        - id: autoincrement
        - model_id: FK
        - ts: timestamp
        - params: input params (JSON)
        - outputs: returned result (JSON)
        - media_paths: separate media (e.g. figures, animations)
        """

        if db_path is None:
            db_path = self.simulation_repo.db_config.database_path
            
        if param_keys is None and rows:
            param_keys = [k for k in rows[0].keys() if k != "error"]

        media_root = Path("results_media") / model_id
        media_root.mkdir(parents=True, exist_ok=True)

        with self.simulation_repo.db_config.get_sqlite_connection() as c:
            ts_now = datetime.datetime.utcnow().isoformat(timespec="seconds") + "Z"

            for idx, row in enumerate(rows):
                params = {k: row[k] for k in param_keys if k in row}
                outputs = {k: v for k, v in row.items() if k not in params}

                media_paths: List[str] = []
                sanitized_outputs = sanitize_metadata(outputs, media_root, media_paths, prefix=f"row{idx}")

                c.execute(
                    "INSERT INTO results (model_id, ts, params, outputs, media_paths) VALUES (?,?,?,?,?)",
                    (
                        model_id,
                        ts_now,
                        json.dumps(params, ensure_ascii=False),
                        json.dumps(sanitized_outputs, ensure_ascii=False),
                        json.dumps(media_paths, ensure_ascii=False),
                    ),
                )
    
    # Implement abstract methods from BaseResultsService
    def load_results_base(self, **kwargs):
        """Load results with filtering options (base implementation)."""
        return super().load_results(
            db_path=kwargs.get("db_path", "mcp.db"),
            model_id=kwargs.get("model_id")
        )
    
    def store_results(self, results: List[Dict[str, Any]], **kwargs) -> None:
        """Store results data."""
        self.store_simulation_results(
            model_id=kwargs["model_id"],
            rows=results,
            param_keys=kwargs.get("param_keys"),
            db_path=kwargs.get("db_path")
        )
    
    def _validate_inputs(self, **kwargs) -> bool:
        """Validate inputs for results operations."""
        return "model_id" in kwargs and kwargs["model_id"]
