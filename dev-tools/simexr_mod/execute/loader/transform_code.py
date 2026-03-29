from dataclasses import dataclass
from pathlib import Path
from typing import Tuple, Dict, Any
import os

# Note: These imports may need adjustment based on actual project structure

from code.refactor.llm_refactor import refactor_to_single_entry
from code.utils.github_utils import fetch_notebook_from_github
from code.utils.notebook_utils import notebook_to_script


@dataclass
class ExternalScriptImporter:
    """
    Full pipeline for an external notebook or script:
      1) If it's a notebook, convert to .py
      2) Refactor to single-entry `simulate(**params)`
      3) Extract metadata (from refactorer)
      4) Iterative smoke-tests & auto-correction
      5) Return (model_id, metadata)
    """
    models_root: Path = Path("external_models")

    def __post_init__(self):
        """Initialize dependencies after dataclass creation."""
        self.models_root.mkdir(parents=True, exist_ok=True)

    def import_and_refactor(
        self,
        source_url: str,
        model_name: str,
        dest_dir: str,
        max_smoke_iters: int = 3,
        llm_model: str = "gpt-5-mini",
    ) -> Tuple[str, Dict[str, Any]]:
        """
        Import and refactor external script/notebook.
        
        Args:
            source_url: URL to the source file
            model_name: Name for the model
            dest_dir: Destination directory
            max_smoke_iters: Maximum smoke test iterations
            llm_model: LLM model to use for refactoring
            
        Returns:
            Tuple of (model_id, metadata)
        """
        print(f"[TRANSFORM_CODE] Starting import_and_refactor for {source_url}")
        print(f"[TRANSFORM_CODE] Fetching notebook from GitHub...")
        nb_path = fetch_notebook_from_github(source_url, dest_dir=dest_dir)
        print(f"[TRANSFORM_CODE] Notebook fetched: {nb_path}")
        
        print(f"[TRANSFORM_CODE] Converting notebook to script...")
        py_path = notebook_to_script(nb_path, output_dir=str(self.models_root))
        print(f"[TRANSFORM_CODE] Script created: {py_path}")

        # Refactor into single entrypoint + extract metadata
        print(f"[TRANSFORM_CODE] Calling refactor_to_single_entry...")
        script_path, metadata = refactor_to_single_entry(Path(py_path))
        print(f"[TRANSFORM_CODE] Refactoring completed. Script path: {script_path}")
        
        # Optionally set/override user-facing name for slugging
        metadata = dict(metadata or {})
        metadata.setdefault("model_name", model_name)
        print(f"[TRANSFORM_CODE] Metadata: {metadata}")

        # Iterative smoke-test + correction loop
        try:
            # Try new structure first (lazy import to avoid circular dependencies)
            from execute.test.simulation_refiner import SimulationRefiner
            refiner = SimulationRefiner(
                script_path=script_path,
                model_name=model_name,
                max_iterations=max_smoke_iters
            )
            model_id = refiner.refine()
        except (NameError, ImportError, ModuleNotFoundError):
            # Fallback - just use the script path as model_id
            import hashlib
            model_id = hashlib.md5(f"{model_name}_{script_path}".encode()).hexdigest()[:12]
        return model_id, metadata
