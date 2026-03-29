"""
Execute module for SimExR - handles simulation execution, testing, and related operations.

This module provides classes for:
- Loading and executing simulation scripts
- Batch running simulations
- Testing and refining simulation code
- Managing dependencies and formatting
- Logging and persistence
"""

# Import core classes for external use
from .base import (
    BaseRunner,
    BaseManager, 
    BaseTester,
    BaseFormatter,
    BaseRepository,
    SimulateLoaderProtocol,
    LoggerProtocol
)

from .loader.simulate_loader import SimulateLoader
from .loader.transform_code import ExternalScriptImporter

from .logging.run_logger import RunLogger

from .model.smoke_test_result import SmokeTestResult

from .persistence.save_script import ScriptRepository

from .run.batch_runner import BatchRunner
from .run.simulation_runner import SimulationRunner

# FixAgent import moved to lazy loading to avoid langchain dependency issues
from .test.simulation_refiner import SimulationRefiner
from .test.smoke_tester import SmokeTester

from .utils.black_formatter import BlackFormatter
from .utils.error_context import ErrorContext
from .utils.json_utlils import json_convert
from .utils.model_utils import make_variant_name
from .utils.python_utils import CodeUtils
from .utils.requirements_manager import RequirementManager

__all__ = [
    # Base classes
    "BaseRunner",
    "BaseManager", 
    "BaseTester",
    "BaseFormatter",
    "BaseRepository",
    "SimulateLoaderProtocol",
    "LoggerProtocol",
    
    # Import classes
    "SimulateLoader",
    "ExternalScriptImporter",
    
    # Logging
    "RunLogger",
    
    # Models
    "SmokeTestResult",
    
    # Persistence
    "ScriptRepository",
    
    # Runners
    "BatchRunner", 
    "SimulationRunner",
    
    # Test classes (FixAgent available via lazy import)
    "SimulationRefiner",
    "SmokeTester",
    
    # Utilities
    "BlackFormatter",
    "ErrorContext",
    "json_convert",
    "make_variant_name",
    "CodeUtils",
    "RequirementManager"
]
