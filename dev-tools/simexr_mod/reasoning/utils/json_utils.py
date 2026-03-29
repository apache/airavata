import json


def _safe_parse(d: str | None) -> dict:
    try:
        val = json.loads(d)
        return val if isinstance(val, dict) else {}
    except Exception:
        return {}