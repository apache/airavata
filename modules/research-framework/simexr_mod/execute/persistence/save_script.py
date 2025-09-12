import json
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Any, Tuple

from slugify import slugify

from execute.base import BaseRepository

# Import database function - adjust path as needed
from db import store_simulation_script



@dataclass
class ScriptRepository(BaseRepository):
    """Persists metadata & simulate.py; registers the script in DB."""
    root: Path = Path("models")

    def save_and_register(self, metadata: Dict[str, Any], code: str) -> Tuple[str, Path]:
        model_slug = slugify(metadata.get("model_name", "unnamed_model"))
        model_dir = self.root / model_slug
        model_dir.mkdir(parents=True, exist_ok=True)

        (model_dir / "metadata.json").write_text(json.dumps(metadata, indent=2))
        (model_dir / "simulate.py").write_text(code)

        model_id = store_simulation_script(
            model_name=model_slug,
            metadata=metadata,
            script_path=str(model_dir / "simulate.py"),
        )
        print(f"[✓] stored model_id = {model_id}  dir = {model_dir}")
        return model_id, model_dir
