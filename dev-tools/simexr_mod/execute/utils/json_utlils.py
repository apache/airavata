import json
from typing import Any

import numpy as np


def json_convert(val: Any) -> Any:
    import datetime as dt

    if isinstance(val, (np.generic,)):
        return val.item()
    if isinstance(val, np.ndarray):
        return [json_convert(v) for v in val.tolist()]
    if isinstance(val, set):
        return [json_convert(v) for v in val]
    if isinstance(val, (dt.datetime, dt.date, dt.time)):
        return val.isoformat()
    if isinstance(val, (bytes, bytearray)):
        return val.decode("utf-8", errors="replace")
    if isinstance(val, complex):
        return {"real": float(val.real), "imag": float(val.imag)}
    if isinstance(val, dict):
        return {k: json_convert(v) for k, v in val.items()}
    if isinstance(val, (list, tuple)):
        return [json_convert(v) for v in val]
    try:
        json.dumps(val)
        return val
    except Exception:
        return str(val)