import ast
from typing import Iterable, List, Tuple, Dict, Optional


class _ParamUsageVisitor(ast.NodeVisitor):
    """
    Finds param usages in simulate(**params)-style scripts:
      • params["k"], params.get("k", default)
      • alias: a = params["k"] or params.get("k"); later reads of `a`
    Notes:
      - We match direct access on Name('params'); this covers almost all real scripts.
      - We record alias reads as kind='alias' to distinguish from raw dict access.
    """
    def __init__(self, param_names: Iterable[str]):
        self.param_names = set(param_names)
        self.references: List[Tuple[str, int, int, str]] = []
        self._alias_from_params: Dict[str, str] = {}

    # Capture aliasing like: a = params['k']  or  a = params.get('k', d)
    def visit_Assign(self, node: ast.Assign):
        rhs_key = self._match_param_subscript(node.value) or self._match_params_get(node.value)
        if rhs_key:
            for t in node.targets:
                if isinstance(t, ast.Name):
                    self._alias_from_params[t.id] = rhs_key
        self.generic_visit(node)

    # Any later read of that alias variable is a usage of the original param key
    def visit_Name(self, node: ast.Name):
        if isinstance(node.ctx, ast.Load) and node.id in self._alias_from_params:
            p = self._alias_from_params[node.id]
            self.references.append((p, node.lineno, node.col_offset, "alias"))
        self.generic_visit(node)

    # Direct subscript access: params['k']
    def visit_Subscript(self, node: ast.Subscript):
        key = self._match_param_subscript(node)
        if key:
            self.references.append((key, node.lineno, node.col_offset, "subscript"))
        self.generic_visit(node)

    # params.get('k', default)
    def visit_Call(self, node: ast.Call):
        key = self._match_params_get(node)
        if key:
            self.references.append((key, node.lineno, node.col_offset, "get"))
        self.generic_visit(node)

    @staticmethod
    def _match_param_subscript(node: ast.AST) -> Optional[str]:
        # Match params['k'] where `params` is a Name
        if isinstance(node, ast.Subscript) and isinstance(node.value, ast.Name) and node.value.id == "params":
            sl = node.slice
            if isinstance(sl, ast.Constant) and isinstance(sl.value, str):
                return sl.value
            if hasattr(ast, "Index") and isinstance(sl, ast.Index) and isinstance(sl.value, ast.Constant):
                if isinstance(sl.value.value, str):
                    return sl.value.value
        return None

    @staticmethod
    def _match_params_get(node: ast.AST) -> Optional[str]:
        # Match params.get('k', ...)
        if isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute) and node.func.attr == "get":
            if isinstance(node.func.value, ast.Name) and node.func.value.id == "params":
                if node.args and isinstance(node.args[0], ast.Constant) and isinstance(node.args[0].value, str):
                    return node.args[0].value
        return None


