"""
Module for injecting parameter overrides into Python simulation code.

This module uses AST manipulation to cleanly inject parameter overrides
into simulation code by:
1. Adding a DEFAULT_OVERRIDES dictionary at the module level
2. Inserting a params merge statement at the beginning of the simulate function
"""

import ast

from core.code.helpers.ast_helpers import build_overrides_assignment, generate_source


class OverrideInjector:
    """
    Handles the AST transformation to inject overrides.

    Separates the transformation logic for better organization.
    """

    def inject(self, tree: ast.Module, assign_overrides: ast.Assign) -> ast.Module:
        """
        Inject overrides into the AST.

        Args:
            tree: AST to modify
            assign_overrides: AST node for DEFAULT_OVERRIDES assignment

        Returns:
            Modified AST
        """
        # Inject at module level
        tree = self._inject_module_overrides(tree, assign_overrides)

        # Inject in simulate function
        tree = self._inject_function_overrides(tree)

        return tree

    def _inject_module_overrides(self, tree: ast.Module, assign_overrides: ast.Assign) -> ast.Module:
        """Inject DEFAULT_OVERRIDES at module level."""
        # Remove any existing DEFAULT_OVERRIDES
        new_body = []
        for node in tree.body:
            if isinstance(node, ast.Assign) and any(
                isinstance(target, ast.Name) and target.id == "DEFAULT_OVERRIDES"
                for target in node.targets
            ):
                continue  # Skip existing DEFAULT_OVERRIDES
            new_body.append(node)

        # Add new DEFAULT_OVERRIDES at the beginning
        new_body.insert(0, assign_overrides)
        tree.body = new_body

        return tree

    def _inject_function_overrides(self, tree: ast.Module) -> ast.Module:
        """Inject params merge in simulate function."""
        for node in tree.body:
            if isinstance(node, ast.FunctionDef) and node.name == "simulate":
                if not self._has_params_merge(node):
                    # Create the merge statement
                    merge_stmt = ast.parse(
                        "params = {**DEFAULT_OVERRIDES, **(params or {})}"
                    ).body[0]

                    # Insert after docstring if present
                    insert_idx = self._get_insert_index(node)
                    node.body.insert(insert_idx, merge_stmt)

        return tree

    def _has_params_merge(self, node: ast.FunctionDef) -> bool:
        """Check if the function already has a params merge statement."""
        for stmt in node.body[:4]:  # Check first few statements
            if isinstance(stmt, ast.Assign) and any(
                isinstance(target, ast.Name) and target.id == "params"
                for target in stmt.targets
            ):
                # Look for DEFAULT_OVERRIDES in the statement
                for name in ast.walk(stmt):
                    if isinstance(name, ast.Name) and name.id == "DEFAULT_OVERRIDES":
                        return True
        return False

    def _get_insert_index(self, node: ast.FunctionDef) -> int:
        """Get index to insert after docstring."""
        # If first statement is a docstring, insert after it
        if (node.body and
            isinstance(node.body[0], ast.Expr) and
            isinstance(getattr(node.body[0], "value", None), ast.Constant) and
            isinstance(node.body[0].value.value, str)):
            return 1
        return 0


def inject_overrides_via_ast(source: str, overrides: Dict[str, Any]) -> str:
    """
    Inject overrides into Python simulation code.

    Adds:
      • module-level DEFAULT_OVERRIDES = {...}
      • First statement inside simulate(**params):
            params = {**DEFAULT_OVERRIDES, **(params or {})}

    Uses AST transformation for clean code manipulation and falls back
    gracefully if code generation fails.

    Args:
        source: Python source code
        overrides: Dictionary of parameter overrides

    Returns:
        Modified Python source code with injected overrides
    """
    if not overrides:
        return source

    # Parse source into AST
    try:
        tree = ast.parse(source)
    except SyntaxError:
        # If source has syntax errors, return original
        return source

    # Build DEFAULT_OVERRIDES dict AST node
    assign_overrides = build_overrides_assignment(overrides)

    # Transform the AST to inject overrides
    transformer = OverrideInjector()
    new_tree = transformer.inject(tree, assign_overrides)
    ast.fix_missing_locations(new_tree)

    # Generate new source code with fallbacks
    return generate_source(new_tree, fallback=source)
