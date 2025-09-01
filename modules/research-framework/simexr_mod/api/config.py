"""
API configuration settings.
"""

from pydantic_settings import BaseSettings
from pathlib import Path


class Settings(BaseSettings):
    """Application settings."""
    
    # Database settings
    database_path: str = str(Path(__file__).parent.parent / "mcp.db")
    
    # API settings
    api_title: str = "SimExR API"
    api_version: str = "1.0.0"
    debug: bool = True
    
    # Execution settings
    max_simulation_timeout: int = 30  # seconds
    max_batch_size: int = 1000
    max_reasoning_steps: int = 20
    
    # File paths
    models_dir: str = str(Path(__file__).parent.parent / "systems" / "models")
    results_media_dir: str = str(Path(__file__).parent.parent / "results_media")
    
    class Config:
        env_file = ".env"
        env_prefix = "SIMEXR_"


settings = Settings()
