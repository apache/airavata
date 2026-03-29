from typing import Any

import pandas as pd


def _is_listy(v: Any) -> bool:
    """Check if value is list-like (list, tuple, numpy array, pandas Series)."""
    import numpy as np
    return isinstance(v, (list, tuple, np.ndarray, pd.Series))


def _to_list(v: Any) -> list:
    """Convert value to list format."""
    import numpy as np
    if isinstance(v, np.ndarray):
        return v.tolist()
    if isinstance(v, pd.Series):
        return v.to_list()
    if isinstance(v, (list, tuple)):
        return list(v)
    # scalar → list of one
    return [v]


def _explode_row(model_id: str, ts: Any, params: dict, outputs: dict) -> pd.DataFrame:
    """
    Explode a single row where some fields in params/outputs may be arrays.
    Strategy:
      - Collect all keys from params + outputs
      - Determine the per-key sequence lengths (only for list-like values)
      - If no list-like values exist → return a single-row dataframe
      - Otherwise, define max_len = max(list lengths)
      - For each key:
          * if list-like: pad/truncate to max_len (pads with None)
          * if scalar: repeat the scalar max_len times
      - Return a dataframe with max_len rows, adding a 'step' index (0..max_len-1)
    """
    # Flatten key space
    all_keys = list(dict.fromkeys([*params.keys(), *outputs.keys()]))

    # Compute lengths for list-like values
    lengths = []
    for k in all_keys:
        v = params.get(k, outputs.get(k, None))  # prefer params; either is fine for length check
        if _is_listy(v):
            lengths.append(len(_to_list(v)))

    if not lengths:
        # No arrays: single-row record
        row = {"model_id": model_id, "ts": ts, "step": 0}
        # Merge params & outputs; params take precedence on key collisions
        merged = {**outputs, **params}
        row.update(merged)
        return pd.DataFrame([row])

    max_len = max(lengths)

    def _series_for(k: str) -> list:
        # prefer params[k] over outputs[k] only for value source when both present
        if k in params:
            v = params[k]
        else:
            v = outputs.get(k, None)

        if _is_listy(v):
            lst = _to_list(v)
            # pad to max_len
            if len(lst) < max_len:
                lst = lst + [None] * (max_len - len(lst))
            elif len(lst) > max_len:
                lst = lst[:max_len]
            return lst
        else:
            # scalar → repeat
            return [v] * max_len

    data = {
        "model_id": [model_id] * max_len,
        "ts": [ts] * max_len,
        "step": list(range(max_len)),
    }
    for k in all_keys:
        data[k] = _series_for(k)

    return pd.DataFrame(data)


