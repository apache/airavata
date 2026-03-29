# db/models/base.py

from datetime import datetime
from typing import Dict, Any
from ..base import BaseModel as AbstractBaseModel


class BaseModel(AbstractBaseModel):
    """Base model class with common fields and methods."""
    
    def __init__(self):
        self.created_at = datetime.utcnow()
        self.updated_at = datetime.utcnow()

    def to_dict(self) -> Dict[str, Any]:
        """Convert model to dictionary representation."""
        return {
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'BaseModel':
        """Create model instance from dictionary."""
        instance = cls()
        if "created_at" in data and data["created_at"]:
            instance.created_at = datetime.fromisoformat(data["created_at"].replace('Z', '+00:00'))
        if "updated_at" in data and data["updated_at"]:
            instance.updated_at = datetime.fromisoformat(data["updated_at"].replace('Z', '+00:00'))
        return instance
    
    def update_timestamp(self):
        """Update the updated_at timestamp."""
        self.updated_at = datetime.utcnow()