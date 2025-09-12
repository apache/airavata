from pathlib import Path
from typing import List, Dict, Optional, Any
import json
from ..config.database import DatabaseConfig
from ..base import BaseRepository as AbstractBaseRepository


class SimulationRepository(AbstractBaseRepository):
    def __init__(self, db_config: DatabaseConfig = None):
        config = db_config or DatabaseConfig()
        super().__init__(db_config=config)
        # For backward compatibility
        self.db_config = config

    def get_simulation_path(self, model_id: str) -> str:
        """
        Return the absolute path to `simulate.py` for the given model_id.

        Args:
            model_id: The ID of the model to find

        Returns:
            str: Path to the simulation script

        Raises:
            KeyError: If the model_id is unknown
        """
        with self.db_config.get_sqlite_connection() as conn:
            row = conn.execute(
                "SELECT script_path FROM simulations WHERE id = ?",
                (model_id,)
            ).fetchone()

            if row is None:
                raise KeyError(f"model_id '{model_id}' not found in DB {self.db_config.database_path}")

            return row["script_path"]

    def store_simulation_results(self, model_id: str, rows: List[dict], param_keys: List[str]) -> None:
        """
        Store simulation results in the results table.

        Args:
            model_id: The ID of the model
            rows: List of result dictionaries from simulation runs
            param_keys: List of parameter names used in the simulation
        """
        with self.db_config.get_sqlite_connection() as conn:
            for row in rows:
                # Split data into params and outputs
                params = self._extract_parameters(row, param_keys)
                outputs = self._extract_results(row, param_keys)

                conn.execute("""
                             INSERT INTO results (model_id, params, outputs, ts)
                             VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                             """, (
                                 model_id,
                                 params,
                                 outputs
                             ))

    @staticmethod
    def _extract_parameters(row: dict, param_keys: List[str]) -> str:
        """Extract and serialize parameters from result row"""
        params = {k: row[k] for k in param_keys if k in row}
        # Include any special fields that start with underscore
        params.update({
            k: v for k, v in row.items()
            if k.startswith('_') and k not in ('_ok', '_error_msg', '_error_type')
        })
        return json.dumps(params)

    @staticmethod
    def _extract_results(row: dict, param_keys: List[str]) -> str:
        """Extract and serialize results, excluding parameters and special fields"""
        results = {
            k: v for k, v in row.items()
            if not k.startswith('_') and k not in param_keys
        }
        # Include error information if present
        if not row.get('_ok', True):
            results['error'] = {
                'type': row.get('_error_type', ''),
                'message': row.get('_error_msg', '')
            }
        return json.dumps(results)
    
    # Implement abstract methods from BaseRepository
    def get(self, id: Any) -> Optional[Any]:
        """Get simulation by ID."""
        try:
            return self.get_simulation_path(str(id))
        except KeyError:
            return None
    
    def list(self, filters: Dict[str, Any] = None) -> List[Any]:
        """List simulations with optional filters."""
        with self.db_config.get_sqlite_connection() as conn:
            query = "SELECT id, name, metadata, script_path FROM simulations"
            params = []
            
            if filters:
                where_clauses = []
                for key, value in filters.items():
                    if key in ['id', 'name']:
                        where_clauses.append(f"{key} = ?")
                        params.append(value)
                
                if where_clauses:
                    query += " WHERE " + " AND ".join(where_clauses)
            
            rows = conn.execute(query, params).fetchall()
            return [dict(row) for row in rows]