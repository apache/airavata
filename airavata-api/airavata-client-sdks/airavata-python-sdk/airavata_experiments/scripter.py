import inspect
import ast
import textwrap
import sys


def scriptize(func):
    # Get the source code of the decorated function
    source_code = textwrap.dedent(inspect.getsource(func))
    func_tree = ast.parse(source_code)

    # Retrieve the module where the function is defined
    module_name = func.__module__
    if module_name in sys.modules:
        module = sys.modules[module_name]
    else:
        raise RuntimeError(f"Cannot find module {module_name} for function {func.__name__}")

    # Attempt to get the module source.
    # If this fails (e.g., in a Jupyter notebook), fallback to an empty module tree.
    try:
        module_source = textwrap.dedent(inspect.getsource(module))
        module_tree = ast.parse(module_source)
    except (TypeError, OSError):
        # In Jupyter (or certain environments), we can't get the module source this way.
        # Use an empty module tree as a fallback.
        module_tree = ast.parse("")

    # Find the function definition node
    func_def = next(
        (node for node in func_tree.body if isinstance(node, ast.FunctionDef)), None)
    if not func_def:
        raise ValueError("No function definition found in func_tree.")

    # ---- NEW: Identify used names in the function body ----
    # We'll walk the function body to collect all names used.
    class NameCollector(ast.NodeVisitor):
        def __init__(self):
            self.used_names = set()

        def visit_Name(self, node):
            self.used_names.add(node.id)
            self.generic_visit(node)

        def visit_Attribute(self, node):
            # This accounts for usage like time.sleep (attribute access)
            # We add 'time' if we see something like time.sleep
            # The top-level name is usually in node.value
            if isinstance(node.value, ast.Name):
                self.used_names.add(node.value.id)
            self.generic_visit(node)

    name_collector = NameCollector()
    name_collector.visit(func_def)
    used_names = name_collector.used_names

    # For imports, we need to consider a few cases:
    # - `import module`
    # - `import module as alias`
    # - `from module import name`
    # We'll keep an import if it introduces at least one name or module referenced by the function.
    def is_import_used(import_node):

        if isinstance(import_node, ast.Import):
            # import something [as alias]
            for alias in import_node.names:
                # If we have something like `import time` and "time" is used,
                # or `import pandas as pd` and "pd" is used, keep it.
                if alias.asname and alias.asname in used_names:
                    return True
                if alias.name.split('.')[0] in used_names:
                    return True
            return False
        elif isinstance(import_node, ast.ImportFrom):
            # from module import name(s)
            # Keep if any of the imported names or their asnames are used
            for alias in import_node.names:
                # Special case: if we have `from module import task_context`, ignore it
                if alias.name == "task_context":
                    return False
                # If from module import x as y, check y; else check x
                if alias.asname and alias.asname in used_names:
                    return True
                if alias.name in used_names:
                    return True
            # Another subtlety: if we have `from time import sleep`
            # and we call `time.sleep()` is that detected?
            # Actually, we already caught attribute usage above, which would add "time" to used_names
            # but not "sleep". If the code does `sleep(n)` directly, then "sleep" is in used_names.
            return False
        return False

    # For other functions, include only if their name is referenced.
    def is_function_used(func_node):
        return func_node.name in used_names

    def wrapper(*args, **kwargs):
        # Bind arguments
        func_signature = inspect.signature(func)
        bound_args = func_signature.bind(*args, **kwargs)
        bound_args.apply_defaults()

        # Convert the original function body to source
        body_source_lines = [ast.unparse(stmt) for stmt in func_def.body]
        body_source_code = "\n".join(body_source_lines)

        # Collect relevant code blocks:
        relevant_code_blocks = []
        for node in module_tree.body:
            if isinstance(node, ast.Import) or isinstance(node, ast.ImportFrom):
                # Include only used imports
                if is_import_used(node):
                    relevant_code_blocks.append(ast.unparse(node).strip())
            elif isinstance(node, ast.FunctionDef):
                # Include only used functions, excluding the decorator itself and the decorated function
                if node.name not in ('task_context', func.__name__) and is_function_used(node):
                    func_code = ast.unparse(node).strip()
                    relevant_code_blocks.append(func_code)

        # Prepare argument assignments
        arg_assignments = []
        for arg_name, arg_value in bound_args.arguments.items():
            # Stringify arguments as before
            if isinstance(arg_value, str):
                arg_assignments.append(f"{arg_name} = {arg_value!r}")
            else:
                arg_assignments.append(f"{arg_name} = {repr(arg_value)}")

        # Combine everything
        combined_code_parts = []
        if relevant_code_blocks:
            combined_code_parts.append("\n\n".join(relevant_code_blocks))
        if arg_assignments:
            if combined_code_parts:
                combined_code_parts.append("")  # blank line before args
            combined_code_parts.extend(arg_assignments)
        if arg_assignments:
            combined_code_parts.append("")  # blank line before body
        combined_code_parts.append(body_source_code)

        combined_code = "\n".join(combined_code_parts).strip()
        return combined_code

    return wrapper
