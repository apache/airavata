# db/models/simulation.py

from datetime import datetime
from typing import Dict, Any, List, Optional
from .base import BaseModel

class SimulationResult(BaseModel):
    def __init__(self, model_id: str, params: Dict[str, Any], results: Dict[str, Any]):
        self.model_id = model_id
        self.params = params
        self.results = results
        self.created_at = datetime.utcnow()
        self.success = True
        self.error_message: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        base_dict = super().to_dict()
        base_dict.update({
            "model_id": self.model_id,
            "params": self.params,
            "results": self.results,
            "success": self.success,
            "error_message": self.error_message
        })
        return base_dict
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'SimulationResult':
        """Create SimulationResult from dictionary."""
        instance = cls(
            model_id=data["model_id"],
            params=data.get("params", {}),
            results=data.get("results", {})
        )
        instance.success = data.get("success", True)
        instance.error_message = data.get("error_message")
        
        # Handle timestamp parsing
        if "created_at" in data and data["created_at"]:
            if isinstance(data["created_at"], str):
                instance.created_at = datetime.fromisoformat(data["created_at"].replace('Z', '+00:00'))
        
        return instance