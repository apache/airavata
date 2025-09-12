"""
Interface definitions for SimExR components.

These interfaces define contracts that concrete implementations must follow,
promoting loose coupling and testability.
"""

from abc import ABC, abstractmethod
from typing import Dict, Any, List, Optional, Protocol, Union
from pathlib import Path
from dataclasses import dataclass
from enum import Enum


class SimulationStatus(Enum):
    """Status of a simulation execution."""
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


@dataclass
class SimulationResult:
    """Standardized simulation result."""
    status: SimulationStatus
    parameters: Dict[str, Any]
    outputs: Dict[str, Any]
    execution_time: float
    error_message: Optional[str] = None
    stdout: str = ""
    stderr: str = ""
    metadata: Dict[str, Any] = None


@dataclass
class SimulationRequest:
    """Standardized simulation request."""
    model_id: str
    parameters: Dict[str, Any]
    execution_options: Dict[str, Any] = None
    priority: int = 0
    timeout: Optional[float] = None


class ISimulationRunner(Protocol):
    """Interface for simulation execution."""
    
    def run(self, request: SimulationRequest) -> SimulationResult:
        """Execute a single simulation."""
        ...
    
    def run_batch(self, requests: List[SimulationRequest]) -> List[SimulationResult]:
        """Execute multiple simulations."""
        ...
    
    def cancel(self, execution_id: str) -> bool:
        """Cancel a running simulation."""
        ...


class ISimulationLoader(Protocol):
    """Interface for loading simulation models."""
    
    def load_model(self, model_id: str) -> Any:
        """Load a simulation model by ID."""
        ...
    
    def validate_model(self, model_path: Path) -> bool:
        """Validate that a model is properly formatted."""
        ...
    
    def get_model_metadata(self, model_id: str) -> Dict[str, Any]:
        """Get metadata for a model."""
        ...


class IResultStore(Protocol):
    """Interface for storing and retrieving simulation results."""
    
    def store_result(self, result: SimulationResult) -> str:
        """Store a simulation result and return an ID."""
        ...
    
    def get_result(self, result_id: str) -> Optional[SimulationResult]:
        """Retrieve a simulation result by ID."""
        ...
    
    def query_results(self, filters: Dict[str, Any]) -> List[SimulationResult]:
        """Query results with filters."""
        ...
    
    def delete_results(self, model_id: str) -> int:
        """Delete all results for a model."""
        ...


class IReasoningAgent(Protocol):
    """Interface for reasoning agents."""
    
    def ask(self, question: str, context: Dict[str, Any]) -> Dict[str, Any]:
        """Ask a question about simulation data."""
        ...
    
    def analyze_results(self, results: List[SimulationResult]) -> Dict[str, Any]:
        """Analyze simulation results."""
        ...
    
    def suggest_parameters(self, model_id: str, objective: str) -> Dict[str, Any]:
        """Suggest parameter values for a given objective."""
        ...


class IEventListener(Protocol):
    """Interface for event listeners."""
    
    def on_simulation_started(self, request: SimulationRequest) -> None:
        """Called when a simulation starts."""
        ...
    
    def on_simulation_completed(self, result: SimulationResult) -> None:
        """Called when a simulation completes."""
        ...
    
    def on_simulation_failed(self, request: SimulationRequest, error: Exception) -> None:
        """Called when a simulation fails."""
        ...


class IExecutionStrategy(Protocol):
    """Interface for execution strategies."""
    
    def execute(self, request: SimulationRequest) -> SimulationResult:
        """Execute a simulation using this strategy."""
        ...
    
    def can_handle(self, request: SimulationRequest) -> bool:
        """Check if this strategy can handle the request."""
        ...
    
    def get_priority(self) -> int:
        """Get the priority of this strategy (higher = more preferred)."""
        ...


class IModelAdapter(Protocol):
    """Interface for adapting different model formats."""
    
    def can_adapt(self, source_format: str, target_format: str) -> bool:
        """Check if this adapter can handle the conversion."""
        ...
    
    def adapt(self, model_content: str, source_format: str, target_format: str) -> str:
        """Convert model from source to target format."""
        ...
    
    def get_supported_formats(self) -> List[str]:
        """Get list of supported formats."""
        ...


class IResourceManager(Protocol):
    """Interface for managing shared resources."""
    
    def acquire_resource(self, resource_type: str, **kwargs) -> Any:
        """Acquire a shared resource."""
        ...
    
    def release_resource(self, resource: Any) -> None:
        """Release a shared resource."""
        ...
    
    def cleanup(self) -> None:
        """Clean up all resources."""
        ...


class IConfigurationProvider(Protocol):
    """Interface for providing configuration."""
    
    def get_config(self, key: str, default: Any = None) -> Any:
        """Get a configuration value."""
        ...
    
    def set_config(self, key: str, value: Any) -> None:
        """Set a configuration value."""
        ...
    
    def get_all_config(self) -> Dict[str, Any]:
        """Get all configuration values."""
        ...


class IValidationRule(Protocol):
    """Interface for validation rules."""
    
    def validate(self, data: Any) -> bool:
        """Validate data against this rule."""
        ...
    
    def get_error_message(self) -> str:
        """Get error message for validation failure."""
        ...


class ISecurityProvider(Protocol):
    """Interface for security operations."""
    
    def authenticate(self, credentials: Dict[str, Any]) -> bool:
        """Authenticate user credentials."""
        ...
    
    def authorize(self, user_id: str, operation: str, resource: str) -> bool:
        """Authorize user operation on resource."""
        ...
    
    def encrypt_sensitive_data(self, data: str) -> str:
        """Encrypt sensitive data."""
        ...
    
    def decrypt_sensitive_data(self, encrypted_data: str) -> str:
        """Decrypt sensitive data."""
        ...
