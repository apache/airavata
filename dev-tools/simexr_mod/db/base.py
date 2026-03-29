"""
Base classes for the db module to provide common interfaces and inheritance.
"""

from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional, Protocol
from pathlib import Path


class DatabaseConfigProtocol(Protocol):
    """Protocol for database configuration implementations."""
    
    database_path: str
    
    def get_sqlite_connection(self):
        """Get a SQLite connection context manager."""
        ...


class RepositoryProtocol(Protocol):
    """Protocol for repository implementations."""
    
    def get(self, id: Any) -> Optional[Any]:
        """Get an entity by ID."""
        ...
    
    def list(self, filters: Dict[str, Any] = None) -> List[Any]:
        """List entities with optional filters."""
        ...


class BaseService(ABC):
    """Base class for all service implementations."""
    
    def __init__(self, repository=None):
        self.repository = repository
    
    @abstractmethod
    def _validate_inputs(self, **kwargs) -> bool:
        """Validate service method inputs."""
        pass


class BaseStorageService(BaseService):
    """Base class for storage service implementations."""
    
    @abstractmethod
    def store(self, data: Dict[str, Any]) -> str:
        """Store data and return identifier."""
        pass
    
    @abstractmethod
    def retrieve(self, identifier: str) -> Dict[str, Any]:
        """Retrieve data by identifier."""
        pass


class BaseResultsService(BaseService):
    """Base class for results service implementations."""
    
    @abstractmethod
    def load_results(self, **kwargs) -> Any:
        """Load results with filtering options."""
        pass
    
    @abstractmethod
    def store_results(self, results: List[Dict[str, Any]], **kwargs) -> None:
        """Store results data."""
        pass


class BaseRepository(ABC):
    """Base class for all repository implementations."""
    
    def __init__(self, db_config=None):
        self.db_config = db_config
    
    @abstractmethod
    def get(self, id: Any) -> Optional[Any]:
        """Get an entity by ID."""
        pass
    
    @abstractmethod
    def list(self, filters: Dict[str, Any] = None) -> List[Any]:
        """List entities with optional filters."""
        pass
    
    def _validate_id(self, id: Any) -> bool:
        """Validate entity ID format."""
        return id is not None and str(id).strip()


class BaseModel(ABC):
    """Base class for all model implementations."""
    
    @abstractmethod
    def to_dict(self) -> Dict[str, Any]:
        """Convert model to dictionary representation."""
        pass
    
    @classmethod
    @abstractmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'BaseModel':
        """Create model instance from dictionary."""
        pass


class DatabaseManagerProtocol(Protocol):
    """Protocol for database manager implementations."""
    
    def setup_database(self) -> None:
        """Initialize database schema."""
        ...
    
    def get_connection(self) -> Any:
        """Get database connection."""
        ...
