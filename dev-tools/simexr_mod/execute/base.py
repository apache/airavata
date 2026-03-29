"""
Base classes for the execute module to provide common interfaces and inheritance.
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Protocol


class SimulateLoaderProtocol(Protocol):
    """Protocol for simulate loader implementations."""
    
    def import_simulate(self, script_path: Path, iu=None) -> Any:
        """Import the simulate function from a script."""
        ...


class LoggerProtocol(Protocol):
    """Protocol for logger implementations."""
    
    @staticmethod
    def get_logger(script_path: Path) -> Any:
        """Get a logger for the given script path."""
        ...
    
    @staticmethod
    def append_jsonl(script_path: Path, record: dict, filename: str = "runs.jsonl") -> None:
        """Append a JSONL record to the log file."""
        ...


@dataclass
class BaseRunner(ABC):
    """Base class for all runner implementations."""
    
    @abstractmethod
    def run(self, *args, **kwargs) -> Any:
        """Run the execution task."""
        pass


@dataclass
class BaseManager(ABC):
    """Base class for all manager implementations."""
    
    @abstractmethod
    def extract(self, content: str) -> List[str]:
        """Extract items from content."""
        pass


@dataclass
class BaseTester(ABC):
    """Base class for all tester implementations."""
    
    @abstractmethod
    def test(self, script_path: Path) -> Any:
        """Test the script at the given path."""
        pass


@dataclass
class BaseFormatter(ABC):
    """Base class for all formatter implementations."""
    
    @abstractmethod
    def format(self, content: str) -> str:
        """Format the given content."""
        pass


@dataclass
class BaseRepository(ABC):
    """Base class for all repository implementations."""
    
    @abstractmethod
    def save_and_register(self, metadata: Dict[str, Any], code: str) -> Any:
        """Save and register the content."""
        pass
