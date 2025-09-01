import nbformat
from nbconvert import PythonExporter
from pathlib import Path
import shutil

def notebook_to_script(notebook_path: str, output_dir: str = "external_models") -> str:
    """
    If `notebook_path` is a Jupyter notebook (.ipynb), convert it to a .py script
    in `output_dir`, returning the script path.
    If it's already a .py file, ensure it's in `output_dir` (copy if needed)
    and return its path.
    """
    src = Path(notebook_path)
    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    # Case 1: Already a Python script
    if src.suffix.lower() == ".py":
        dest = out_dir / src.name
        # copy only if not already in the target dir
        if src.resolve() != dest.resolve():
            shutil.copy2(src, dest)
        return str(dest)

    # Case 2: Jupyter notebook → Python script
    if src.suffix.lower() == ".ipynb":
        nb = nbformat.read(src, as_version=4)
        exporter = PythonExporter()
        script_source, _ = exporter.from_notebook_node(nb)

        py_path = out_dir / (src.stem + ".py")
        py_path.write_text(script_source)
        return str(py_path)

    # Unsupported extension
    raise ValueError(f"Cannot convert '{notebook_path}': unsupported extension '{src.suffix}'")
