from typing import List, Any, Dict
import re

from core.code.models.param_ref import ParamRef


def group_param_refs(refs: List[ParamRef]) -> Dict[str, List[Dict[str, Any]]]:
    grouped: Dict[str, List[Dict[str, Any]]] = {}
    for r in refs:
        grouped.setdefault(r.param, []).append({"line": r.line, "col": r.col, "kind": r.kind})
    for k in grouped:
        grouped[k].sort(key=lambda x: (x["line"], x["col"]))
    return grouped

def _coerce_literal(val: Any) -> Any:
    """Coerce editor string values to float/int if possible; support simple fractions like '8/3'."""
    if val is None:
        return None
    if isinstance(val, (int, float)):
        return val
    s = str(val).strip()
    if s == "":
        return ""
    m = re.fullmatch(r"(-?\d+(?:\.\d*)?)\s*/\s*(\d+(?:\.\d*)?)", s)
    if m:
        num, den = map(float, m.groups())
        return num / den
    # try int then float
    try:
        if re.fullmatch(r"-?\d+", s):
            return int(s)
        return float(s)
    except Exception:
        return s


def _grab_line_context(source_lines: List[str], line: int, col: int, pad: int = 120) -> str:
    if 1 <= line <= len(source_lines):
        s = source_lines[line - 1].rstrip("\n")
        caret = " " * max(col, 0) + "^"
        if len(s) > pad:
            s = s[:pad] + " ..."
        return f"{s}\n{caret}"
    return ""