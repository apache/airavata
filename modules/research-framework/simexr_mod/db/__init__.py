# db/__init__.py
import sqlite3
from pathlib import Path
from typing import Optional

from .config.database import DatabaseConfig
from .services.store import StorageService
from .services.results import ResultsService
from .repositories.simulation import SimulationRepository


class Database:
    def __init__(self, config: Optional[DatabaseConfig] = None):
        self.config = config or DatabaseConfig()
        self.setup_database()

        # Initialize repositories and services
        self.simulation_repository = SimulationRepository(self.config)
        self.storage_service = StorageService(self.simulation_repository)
        self.results_service = ResultsService(self.simulation_repository)

    def _conn(self, db_path: str | Path = None) -> sqlite3.Connection:
        if db_path is None:
            db_path = self.config.database_path
        conn = sqlite3.connect(str(db_path))
        conn.row_factory = sqlite3.Row
        return conn

    def setup_database(self):
        """Initialize database tables"""
        with self.config.get_sqlite_connection() as conn:
            conn.execute("""
                         CREATE TABLE IF NOT EXISTS simulations
                         (
                             id
                             TEXT
                             PRIMARY
                             KEY,
                             name
                             TEXT
                             NOT
                             NULL,
                             metadata
                             TEXT,
                             script_path
                             TEXT
                             NOT
                             NULL,
                             created_at
                             TIMESTAMP
                             DEFAULT
                             CURRENT_TIMESTAMP
                         );
                         """)

            conn.execute("""
                         CREATE TABLE IF NOT EXISTS simulation_results
                         (
                             id
                             INTEGER
                             PRIMARY
                             KEY
                             AUTOINCREMENT,
                             model_id
                             TEXT
                             NOT
                             NULL,
                             params
                             TEXT
                             NOT
                             NULL,
                             results
                             TEXT
                             NOT
                             NULL,
                             created_at
                             TIMESTAMP
                             DEFAULT
                             CURRENT_TIMESTAMP,
                             FOREIGN
                             KEY
                         (
                             model_id
                         ) REFERENCES simulations
                         (
                             id
                         )
                             );
                         """)

            conn.execute("""
                         CREATE TABLE IF NOT EXISTS reasoning_agent
                         (
                             id
                             INTEGER
                             PRIMARY
                             KEY
                             AUTOINCREMENT,
                             model_id
                             TEXT
                             NOT
                             NULL,
                             question
                             TEXT
                             NOT
                             NULL,
                             answer
                             TEXT
                             NOT
                             NULL,
                             images
                             TEXT,
                             ts
                             TIMESTAMP
                             DEFAULT
                             CURRENT_TIMESTAMP,
                             FOREIGN
                             KEY
                         (
                             model_id
                         ) REFERENCES simulations
                         (
                             id
                         )
                             );
                         """)

    @classmethod
    def create_default(cls) -> 'Database':
        """Create a database instance with default configuration"""
        config = DatabaseConfig(
            dialect="sqlite",
            database_path=str(Path(__file__).parent.parent / "mcp.db")
        )
        return cls(config)


# Create a default database instance
default_db = Database.create_default()

# Export functions for backward compatibility
def store_simulation_script(model_name: str, metadata: dict, script_path: str) -> str:
    """Store a simulation script and return model ID."""
    return default_db.storage_service.store_simulation_script(model_name, metadata, script_path)

def get_simulation_path(model_id: str, db_path: str = None) -> str:
    """Get the path to a simulation script."""
    return default_db.simulation_repository.get_simulation_path(model_id)

def store_simulation_results(model_id: str, rows: list, param_keys: list, db_path: str = None) -> None:
    """Store simulation results."""
    default_db.simulation_repository.store_simulation_results(model_id, rows, param_keys)

def store_report(model_id: str, question: str, answer: str, images: list) -> None:
    """Store a reasoning report."""
    from .services.reasoning import ReasoningService
    service = ReasoningService()
    service.store_report(model_id, question, answer, images)

__all__ = [
    # Classes
    'Database',
    'DatabaseConfig',
    'StorageService',
    'ResultsService',
    'SimulationRepository',
    'default_db',
    
    # Compatibility functions
    'store_simulation_script',
    'get_simulation_path', 
    'store_simulation_results',
    'store_report'
]