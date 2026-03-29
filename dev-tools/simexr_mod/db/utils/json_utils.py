import datetime
import json
from pathlib import Path
from typing import List, Dict, Any
import pandas as pd
import numpy as np


def _safe_parse(x: Any) -> dict:
    if isinstance(x, dict):
        return x
    if x is None or (isinstance(x, float) and pd.isna(x)):
        return {}
    try:
        return json.loads(x)
    except Exception:
        return {}

def _is_listy(v: Any) -> bool:
    # Treat numpy arrays, lists, tuples, Series as list-like
    return isinstance(v, (list, tuple, np.ndarray, pd.Series))


def _to_list(v: Any) -> list:
    if isinstance(v, np.ndarray):
        return v.tolist()
    if isinstance(v, pd.Series):
        return v.to_list()
    if isinstance(v, (list, tuple)):
        return list(v)
    # scalar → list of one
    return [v]

# def store_simulation_results(
#         model_id: str,
#         rows: List[Dict[str, Any]],
#         param_keys: List[str] | None = None,
#         db_path: str | Path = DB_DEFAULT,
# ) -> None:
#     """
#     Persist experiment result rows with sanitized outputs and saved media.
#
#     DB schema:
#     - id: autoincrement
#     - model_id: FK
#     - ts: timestamp
#     - params: input params (JSON)
#     - outputs: returned result (JSON)
#     - media_paths: separate media (e.g. figures, animations)
#     """
#
#     if param_keys is None and rows:
#         param_keys = [k for k in rows[0].keys() if k != "error"]
#
#     media_root = Path("results_media") / model_id
#     media_root.mkdir(parents=True, exist_ok=True)
#
#     with _conn(db_path) as c:
#
#         ts_now = datetime.utcnow().isoformat(timespec="seconds") + "Z"
#
#         for idx, row in enumerate(rows):
#             params = {k: row[k] for k in param_keys if k in row}
#             outputs = {k: v for k, v in row.items() if k not in params}
#
#             media_paths: List[str] = []
#             sanitized_outputs = sanitize_metadata(outputs, media_root, media_paths, prefix=f"row{idx}")
#
#             c.execute(
#                 "INSERT INTO results (model_id, ts, params, outputs, media_paths) VALUES (?,?,?,?,?)",
#                 (
#                     model_id,
#                     ts_now,
#                     json.dumps(params, ensure_ascii=False),
#                     json.dumps(sanitized_outputs, ensure_ascii=False),
#                     json.dumps(media_paths, ensure_ascii=False),
#                 ),
#             )
