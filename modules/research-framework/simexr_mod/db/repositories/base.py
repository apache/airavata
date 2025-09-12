# db/repositories/base.py

from typing import Any, Dict, List, Optional
from sqlalchemy.orm import Session
from ..base import BaseRepository as AbstractBaseRepository
from ..models.base import BaseModel


class BaseRepository(AbstractBaseRepository):
    """SQLAlchemy-based repository implementation."""
    
    def __init__(self, session: Session = None, db_config=None):
        super().__init__(db_config=db_config)
        self.session = session

    def get(self, id: Any) -> Optional[BaseModel]:
        """Get entity by ID - to be implemented by subclasses."""
        if not self._validate_id(id):
            return None
        raise NotImplementedError("Subclasses must implement get method")

    def list(self, filters: Dict[str, Any] = None) -> List[BaseModel]:
        """List entities with filters - to be implemented by subclasses."""
        raise NotImplementedError("Subclasses must implement list method")

    def create(self, model: BaseModel) -> BaseModel:
        """Create new entity - to be implemented by subclasses."""
        if not isinstance(model, BaseModel):
            raise ValueError("Model must be an instance of BaseModel")
        raise NotImplementedError("Subclasses must implement create method")

    def update(self, model: BaseModel) -> BaseModel:
        """Update existing entity - to be implemented by subclasses."""
        if not isinstance(model, BaseModel):
            raise ValueError("Model must be an instance of BaseModel")
        model.update_timestamp()
        raise NotImplementedError("Subclasses must implement update method")

    def delete(self, id: Any) -> None:
        """Delete entity by ID - to be implemented by subclasses."""
        if not self._validate_id(id):
            raise ValueError("Invalid ID provided")
        raise NotImplementedError("Subclasses must implement delete method")