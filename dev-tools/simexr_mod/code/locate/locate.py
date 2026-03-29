import ast
import re
from dataclasses import dataclass
from typing import List, Dict, Any, Iterable

from core.code.locate.param_usage_visitor import _ParamUsageVisitor
from core.code.models.param_ref import ParamRef
from core.code.helpers.locate_helpers import _grab_line_context


def locate_param_references_from_source(source: str, param_names: Iterable[str]) -> List[ParamRef]:
    lines = source.splitlines(True)
    try:
        tree = ast.parse(source)
    except Exception:
        return []
    visitor = _ParamUsageVisitor(param_names)
    visitor.visit(tree)
    refs: List[ParamRef] = []
    for p, ln, col, kind in visitor.references:
        ctx = _grab_line_context(lines, ln, col)
        refs.append(ParamRef(param=p, line=ln, col=col, kind=kind, context=ctx))
    # stable ordering by param then location
    refs.sort(key=lambda r: (r.param, r.line, r.col))
    return refs


