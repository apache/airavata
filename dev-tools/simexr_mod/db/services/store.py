# db/services/store.py

import json
import sqlite3
from pathlib import Path
from typing import Dict, Any, List, Union

from ..config.database import DatabaseConfig
from ..repositories.simulation import SimulationRepository
from ..base import BaseStorageService

class StorageService(BaseStorageService):
    def __init__(self, simulation_repo: SimulationRepository):
        super().__init__(repository=simulation_repo)
        self.simulation_repo = simulation_repo

    def store_simulation_script(
            self,
            model_name: str,
            metadata: Dict[str, Any],
            script_path: Union[str, Path],
            db_path: Union[str, Path] = None,
    ) -> str:
        """
        Store a simulation script entry if it doesn't already exist.
        Returns a unique model_id derived from model name + script content.
        """
        if db_path is None:
            db_path = self.simulation_repo.db_config.database_path
            
        script_path = str(script_path)
        from ..utils.hash_utils import generate_model_id
        model_id = generate_model_id(model_name, script_path)

        with self.simulation_repo.db_config.get_sqlite_connection() as c:
            # Ensure table exists
            c.execute(
                """CREATE TABLE IF NOT EXISTS simulations
                   (
                       id
                       TEXT
                       PRIMARY
                       KEY,
                       name
                       TEXT,
                       metadata
                       TEXT,
                       script_path
                       TEXT,
                       media_paths
                       TEXT
                   )"""
            )

            # Check if the exact model_id already exists
            existing = c.execute(
                "SELECT id FROM simulations WHERE id = ?", (model_id,)
            ).fetchone()

            if existing:
                print(f"[✓] Simulation already exists: {model_id}")
                return model_id

            # Insert new unique entry
            c.execute(
                """INSERT INTO simulations (id, name, metadata, script_path)
                   VALUES (?, ?, ?, ?)""",
                (model_id, model_name, json.dumps(metadata), script_path),
            )
            print(f"[+] Stored new simulation: {model_id}")

        return model_id

    def get_model_metadata(self, model_id: str, db_path: str | Path = None) -> dict:
        if db_path is None:
            db_path = self.simulation_repo.db_config.database_path
        with self.simulation_repo.db_config.get_sqlite_connection() as c:
            row = c.execute(
                "SELECT metadata FROM simulations WHERE id = ?", (model_id,)
            )
            row = row.fetchone()
            if row is None:
                raise ValueError(f"No metadata found for model_id={model_id}")
            return json.loads(row["metadata"])

    def get_simulation_script_code(self, model_id: str, db_path: str = None) -> str:
        """
        Fetch the saved path for this model_id, read that file,
        dedent it, and return the actual Python code as a string.
        """
        if db_path is None:
            db_path = self.simulation_repo.db_config.database_path
            
        import textwrap
        with self.simulation_repo.db_config.get_sqlite_connection() as conn:
            row = conn.execute("SELECT script_path FROM simulations WHERE id = ?", (model_id,)).fetchone()

        if not row:
            raise ValueError(f"No script found for model_id={model_id!r}")

        script_path = row[0]
        code = Path(script_path).read_text(encoding="utf-8")
        return textwrap.dedent(code)
    
    # Implement abstract methods from BaseStorageService
    def store(self, data: Dict[str, Any]) -> str:
        """Store simulation data and return model ID."""
        return self.store_simulation_script(
            model_name=data["name"],
            metadata=data.get("metadata", {}),
            script_path=data["script_path"]
        )
    
    def retrieve(self, identifier: str) -> Dict[str, Any]:
        """Retrieve simulation data by model ID."""
        metadata = self.get_model_metadata(identifier)
        script_code = self.get_simulation_script_code(identifier)
        return {
            "id": identifier,
            "metadata": metadata,
            "script_code": script_code
        }
    
    def _validate_inputs(self, **kwargs) -> bool:
        """Validate inputs for storage operations."""
        required_fields = ["model_name", "script_path"]
        return all(field in kwargs and kwargs[field] for field in required_fields)