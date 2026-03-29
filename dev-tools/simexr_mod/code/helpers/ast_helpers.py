"""
Helper functions for AST manipulation and source code generation.

This module provides utilities for working with Python's Abstract Syntax Tree
and generating source code from AST nodes.
"""

import ast
import json
from typing import Dict, Any


def build_overrides_assignment(overrides: Dict[str, Any]) -> ast.Assign:
    """
    Build an AST assignment node for DEFAULT_OVERRIDES.

    Handles various data types, converting complex types to JSON strings.

    Args:
        overrides: Dictionary of parameter overrides

    Returns:
        AST assignment node
    """
    keys, values = [], []

    for k, v in overrides.items():
        keys.append(ast.Constant(value=str(k)))

        # Handle different types of values
        if isinstance(v, (int, float, str, bool)) or v is None:
            values.append(ast.Constant(value=v))
        else:
            # Convert complex types to JSON strings
            values.append(ast.Constant(value=json.dumps(v)))

    return ast.Assign(
        targets=[ast.Name(id="DEFAULT_OVERRIDES", ctx=ast.Store())],
        value=ast.Dict(keys=keys, values=values),
    )


def generate_source(tree: ast.AST, fallback: str) -> str:
    """
    Generate source code from AST with multiple fallback methods.

    Tries multiple approaches to generate source code:
    1. ast.unparse (Python 3.9+)
    2. astor.to_source (if available)
    3. black formatting (if available)
    4. Original source (last resort)

    Args:
        tree: AST to convert to source
        fallback: Original source to use as fallback

    Returns:
        Generated source code
    """
    new_code = None

    # Try ast.unparse (Python 3.9+)
    try:
        new_code = ast.unparse(tree)
    except Exception:
        pass

    # Try astor if ast.unparse failed
    if new_code is None:
        try:
            import astor
            new_code = astor.to_source(tree)
        except Exception:
            return fallback  # Last resort fallback

    # Optional formatting with black
    try:
        import black
        new_code = black.format_str(new_code, mode=black.Mode())
    except Exception:
        pass

    return new_code or fallback


def find_function_by_name(tree: ast.Module, name: str) -> ast.FunctionDef:
    """
    Find a function in an AST by name.

    Args:
        tree: AST module node
        name: Function name to find

    Returns:
        Function definition node or None if not found
    """
    for node in tree.body:
        if isinstance(node, ast.FunctionDef) and node.name == name:
            return node
    return None


def has_docstring(node: ast.FunctionDef) -> bool:
    """
    Check if a function has a docstring.

    Args:
        node: Function definition node

    Returns:
        True if the function has a docstring
    """
    return (node.body and
            isinstance(node.body[0], ast.Expr) and
            isinstance(getattr(node.body[0], "value", None), ast.Constant) and
            isinstance(node.body[0].value.value, str))