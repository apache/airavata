"""
Implementation of key design patterns for SimExR.

This module provides concrete implementations of various design patterns
to improve code organization and maintainability.
"""

import threading
import weakref
import logging
from abc import ABC, abstractmethod
from typing import Dict, Any, List, Optional, Type, Callable, Union
from pathlib import Path
from dataclasses import dataclass, field
from enum import Enum
import time
import uuid

from .interfaces import (
    ISimulationRunner, ISimulationLoader, IResultStore, IReasoningAgent,
    IEventListener, IExecutionStrategy, IModelAdapter, IResourceManager,
    SimulationRequest, SimulationResult, SimulationStatus
)


# ===== FACTORY PATTERN =====

class ComponentType(Enum):
    """Types of components that can be created by the factory."""
    SIMULATION_RUNNER = "simulation_runner"
    RESULT_STORE = "result_store"
    REASONING_AGENT = "reasoning_agent"
    MODEL_LOADER = "model_loader"
    EXECUTION_STRATEGY = "execution_strategy"


class SimulationFactory:
    """Factory for creating simulation-related components."""
    
    def __init__(self):
        self._creators: Dict[ComponentType, Callable] = {}
        self._instances: Dict[str, Any] = {}
    
    def register_creator(self, component_type: ComponentType, creator: Callable):
        """Register a creator function for a component type."""
        self._creators[component_type] = creator
    
    def create(self, component_type: ComponentType, **kwargs) -> Any:
        """Create a component of the specified type."""
        if component_type not in self._creators:
            raise ValueError(f"No creator registered for {component_type}")
        
        creator = self._creators[component_type]
        return creator(**kwargs)
    
    def create_singleton(self, component_type: ComponentType, instance_id: str, **kwargs) -> Any:
        """Create or retrieve a singleton instance."""
        if instance_id in self._instances:
            return self._instances[instance_id]
        
        instance = self.create(component_type, **kwargs)
        self._instances[instance_id] = instance
        return instance
    
    def get_registered_types(self) -> List[ComponentType]:
        """Get list of registered component types."""
        return list(self._creators.keys())


# ===== STRATEGY PATTERN =====

class LocalExecutionStrategy:
    """Strategy for executing simulations locally."""
    
    def __init__(self, timeout: float = 30.0):
        self.timeout = timeout
        self.logger = logging.getLogger("LocalExecutionStrategy")
        self.logger.setLevel(logging.INFO)
    
    def execute(self, request: SimulationRequest) -> SimulationResult:
        """Execute simulation locally."""
        self.logger.info(f"[LOCAL_EXECUTION] Starting local execution for model {request.model_id}")
        self.logger.info(f"[LOCAL_EXECUTION] Parameters: {request.parameters}")
        self.logger.info(f"[LOCAL_EXECUTION] Timeout: {self.timeout}s")
        
        start_time = time.time()
        
        try:
            # Import here to avoid circular dependencies
            from execute.run.simulation_runner import SimulationRunner
            from db import get_simulation_path
            
            self.logger.info(f"[LOCAL_EXECUTION] Getting simulation path for model {request.model_id}")
            script_path = Path(get_simulation_path(request.model_id))
            self.logger.info(f"[LOCAL_EXECUTION] Script path: {script_path}")
            
            self.logger.info(f"[LOCAL_EXECUTION] Creating SimulationRunner")
            runner = SimulationRunner()
            
            self.logger.info(f"[LOCAL_EXECUTION] Running simulation with runner")
            result = runner.run(script_path, request.parameters)
            
            execution_time = time.time() - start_time
            self.logger.info(f"[LOCAL_EXECUTION] Simulation completed in {execution_time:.3f}s")
            
            success = result.get("_ok", False)
            self.logger.info(f"[LOCAL_EXECUTION] Success status: {success}")
            
            # Log result preview
            if success:
                self.logger.info(f"[LOCAL_EXECUTION] Creating successful SimulationResult")
                self._log_final_result_preview(result)
            else:
                self.logger.warning(f"[LOCAL_EXECUTION] Creating failed SimulationResult")
            
            return SimulationResult(
                status=SimulationStatus.COMPLETED if success else SimulationStatus.FAILED,
                parameters=request.parameters,
                outputs={k: v for k, v in result.items() if not k.startswith("_")},
                execution_time=execution_time,
                stdout=result.get("_stdout", ""),
                stderr=result.get("_stderr", ""),
                error_message=result.get("_error_msg") if not success else None
            )
            
        except Exception as e:
            execution_time = time.time() - start_time
            self.logger.error(f"[LOCAL_EXECUTION] Execution failed after {execution_time:.3f}s: {str(e)}")
            self.logger.error(f"[LOCAL_EXECUTION] Error type: {type(e).__name__}")
            
            return SimulationResult(
                status=SimulationStatus.FAILED,
                parameters=request.parameters,
                outputs={},
                execution_time=execution_time,
                error_message=str(e)
            )
    
    def can_handle(self, request: SimulationRequest) -> bool:
        """Check if this strategy can handle the request."""
        return True  # Local execution can handle any request
    
    def get_priority(self) -> int:
        """Get priority (lower = higher priority)."""
        return 10
    
    def _log_final_result_preview(self, result: Dict[str, Any]) -> None:
        """Log a preview of the final simulation results."""
        self.logger.info(f"[LOCAL_EXECUTION] === FINAL RESULT SUMMARY ===")
        
        # Show key metrics
        if 'success' in result:
            self.logger.info(f"[LOCAL_EXECUTION] Success: {result['success']}")
        
        if 'solver_message' in result:
            self.logger.info(f"[LOCAL_EXECUTION] Solver: {result['solver_message']}")
        
        # Show data sizes
        for key in ['t', 'x', 'y']:
            if key in result and isinstance(result[key], (list, tuple)):
                self.logger.info(f"[LOCAL_EXECUTION] {key.upper()} data points: {len(result[key])}")
        
        # Show grid info if available
        for key in ['x_grid', 'y_grid', 'u_grid', 'v_grid']:
            if key in result and isinstance(result[key], (list, tuple)):
                if len(result[key]) > 0 and isinstance(result[key][0], (list, tuple)):
                    self.logger.info(f"[LOCAL_EXECUTION] {key.upper()} grid: {len(result[key])}x{len(result[key][0])}")
        
        # Show key parameters
        for key in ['mu', 'z0', 'eval_time', 't_iteration', 'grid_points', 'mgrid_size']:
            if key in result:
                self.logger.info(f"[LOCAL_EXECUTION] {key}: {result[key]}")
        
        self.logger.info(f"[LOCAL_EXECUTION] === END FINAL RESULT SUMMARY ===")


class RemoteExecutionStrategy:
    """Strategy for executing simulations remotely (placeholder)."""
    
    def __init__(self, endpoint: str):
        self.endpoint = endpoint
    
    def execute(self, request: SimulationRequest) -> SimulationResult:
        """Execute simulation remotely."""
        # Placeholder implementation
        raise NotImplementedError("Remote execution not yet implemented")
    
    def can_handle(self, request: SimulationRequest) -> bool:
        """Check if this strategy can handle the request."""
        return False  # Not implemented yet
    
    def get_priority(self) -> int:
        """Get priority."""
        return 5  # Higher priority than local if available


class ExecutionStrategyManager:
    """Manages different execution strategies."""
    
    def __init__(self):
        self.strategies: List[IExecutionStrategy] = []
    
    def add_strategy(self, strategy: IExecutionStrategy):
        """Add an execution strategy."""
        self.strategies.append(strategy)
        # Sort by priority (lower number = higher priority)
        self.strategies.sort(key=lambda s: s.get_priority())
    
    def execute(self, request: SimulationRequest) -> SimulationResult:
        """Execute using the best available strategy."""
        for strategy in self.strategies:
            if strategy.can_handle(request):
                return strategy.execute(request)
        
        raise RuntimeError("No execution strategy available for this request")


# ===== OBSERVER PATTERN =====

class SimulationEvent:
    """Event data for simulation notifications."""
    
    def __init__(self, event_type: str, data: Dict[str, Any]):
        self.event_type = event_type
        self.data = data
        self.timestamp = time.time()


class SimulationSubject:
    """Subject that notifies observers of simulation events."""
    
    def __init__(self):
        self._observers: List[IEventListener] = []
    
    def attach(self, observer: IEventListener):
        """Attach an observer."""
        if observer not in self._observers:
            self._observers.append(observer)
    
    def detach(self, observer: IEventListener):
        """Detach an observer."""
        if observer in self._observers:
            self._observers.remove(observer)
    
    def notify_started(self, request: SimulationRequest):
        """Notify all observers that a simulation started."""
        for observer in self._observers:
            try:
                observer.on_simulation_started(request)
            except Exception as e:
                print(f"Observer notification failed: {e}")
    
    def notify_completed(self, result: SimulationResult):
        """Notify all observers that a simulation completed."""
        for observer in self._observers:
            try:
                observer.on_simulation_completed(result)
            except Exception as e:
                print(f"Observer notification failed: {e}")
    
    def notify_failed(self, request: SimulationRequest, error: Exception):
        """Notify all observers that a simulation failed."""
        for observer in self._observers:
            try:
                observer.on_simulation_failed(request, error)
            except Exception as e:
                print(f"Observer notification failed: {e}")


class LoggingObserver:
    """Observer that logs simulation events."""
    
    def __init__(self, log_file: Optional[Path] = None):
        self.log_file = log_file
    
    def on_simulation_started(self, request: SimulationRequest):
        """Log simulation start."""
        message = f"Simulation started: {request.model_id} with params {request.parameters}"
        self._log(message)
    
    def on_simulation_completed(self, result: SimulationResult):
        """Log simulation completion."""
        message = f"Simulation completed: {result.status.value} in {result.execution_time:.2f}s"
        self._log(message)
    
    def on_simulation_failed(self, request: SimulationRequest, error: Exception):
        """Log simulation failure."""
        message = f"Simulation failed: {request.model_id} - {str(error)}"
        self._log(message)
    
    def _log(self, message: str):
        """Write log message."""
        timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
        full_message = f"[{timestamp}] {message}"
        
        if self.log_file:
            with open(self.log_file, 'a') as f:
                f.write(full_message + "\n")
        else:
            print(full_message)


# ===== COMMAND PATTERN =====

class Command(ABC):
    """Abstract command interface."""
    
    @abstractmethod
    def execute(self) -> Any:
        """Execute the command."""
        pass
    
    @abstractmethod
    def undo(self) -> Any:
        """Undo the command."""
        pass


class RunSimulationCommand(Command):
    """Command to run a simulation."""
    
    def __init__(self, runner: ISimulationRunner, request: SimulationRequest):
        self.runner = runner
        self.request = request
        self.result: Optional[SimulationResult] = None
    
    def execute(self) -> SimulationResult:
        """Execute the simulation."""
        self.result = self.runner.run(self.request)
        return self.result
    
    def undo(self) -> None:
        """Undo not applicable for simulation execution."""
        pass


class StoreModelCommand(Command):
    """Command to store a simulation model."""
    
    def __init__(self, model_name: str, metadata: Dict[str, Any], script_content: str):
        self.model_name = model_name
        self.metadata = metadata
        self.script_content = script_content
        self.model_id: Optional[str] = None
    
    def execute(self) -> str:
        """Store the model."""
        from db import store_simulation_script
        import tempfile
        
        # Create temporary script file
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write(self.script_content)
            temp_path = f.name
        
        try:
            self.model_id = store_simulation_script(
                model_name=self.model_name,
                metadata=self.metadata,
                script_path=temp_path
            )
            return self.model_id
        finally:
            Path(temp_path).unlink(missing_ok=True)
    
    def undo(self) -> None:
        """Delete the stored model."""
        if self.model_id:
            # Implementation would delete the model from database
            pass


class CommandInvoker:
    """Invoker that executes commands and maintains history."""
    
    def __init__(self):
        self.history: List[Command] = []
    
    def execute_command(self, command: Command) -> Any:
        """Execute a command and add to history."""
        result = command.execute()
        self.history.append(command)
        return result
    
    def undo_last(self) -> None:
        """Undo the last command."""
        if self.history:
            command = self.history.pop()
            command.undo()


# ===== BUILDER PATTERN =====

class SimulationConfigBuilder:
    """Builder for creating complex simulation configurations."""
    
    def __init__(self):
        self.reset()
    
    def reset(self):
        """Reset the builder state."""
        self._config = {
            'model_id': None,
            'parameters': {},
            'execution_options': {},
            'validation_rules': [],
            'observers': [],
            'strategies': []
        }
        return self
    
    def set_model(self, model_id: str):
        """Set the simulation model."""
        self._config['model_id'] = model_id
        return self
    
    def add_parameter(self, name: str, value: Any):
        """Add a simulation parameter."""
        self._config['parameters'][name] = value
        return self
    
    def add_parameters(self, parameters: Dict[str, Any]):
        """Add multiple simulation parameters."""
        self._config['parameters'].update(parameters)
        return self
    
    def set_execution_option(self, name: str, value: Any):
        """Set an execution option."""
        self._config['execution_options'][name] = value
        return self
    
    def set_timeout(self, timeout: float):
        """Set execution timeout."""
        self._config['execution_options']['timeout'] = timeout
        return self
    
    def set_priority(self, priority: int):
        """Set execution priority."""
        self._config['execution_options']['priority'] = priority
        return self
    
    def add_observer(self, observer: IEventListener):
        """Add an event observer."""
        self._config['observers'].append(observer)
        return self
    
    def add_strategy(self, strategy: IExecutionStrategy):
        """Add an execution strategy."""
        self._config['strategies'].append(strategy)
        return self
    
    def build(self) -> Dict[str, Any]:
        """Build the final configuration."""
        if not self._config['model_id']:
            raise ValueError("Model ID is required")
        
        config = self._config.copy()
        self.reset()
        return config


# ===== SINGLETON PATTERN =====

class SingletonMeta(type):
    """Metaclass for creating singleton instances."""
    
    _instances = {}
    _lock = threading.Lock()
    
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            with cls._lock:
                if cls not in cls._instances:
                    cls._instances[cls] = super().__call__(*args, **kwargs)
        return cls._instances[cls]


class ResourceManager(metaclass=SingletonMeta):
    """Singleton resource manager for shared resources."""
    
    def __init__(self):
        if hasattr(self, '_initialized'):
            return
        
        self._resources: Dict[str, Any] = {}
        self._locks: Dict[str, threading.Lock] = {}
        self._initialized = True
    
    def get_resource(self, resource_id: str, factory: Callable = None) -> Any:
        """Get or create a resource."""
        if resource_id not in self._resources:
            if factory is None:
                raise ValueError(f"Resource {resource_id} not found and no factory provided")
            
            if resource_id not in self._locks:
                self._locks[resource_id] = threading.Lock()
            
            with self._locks[resource_id]:
                if resource_id not in self._resources:
                    self._resources[resource_id] = factory()
        
        return self._resources[resource_id]
    
    def set_resource(self, resource_id: str, resource: Any):
        """Set a resource."""
        self._resources[resource_id] = resource
    
    def release_resource(self, resource_id: str):
        """Release a resource."""
        if resource_id in self._resources:
            resource = self._resources.pop(resource_id)
            if hasattr(resource, 'cleanup'):
                resource.cleanup()
    
    def cleanup_all(self):
        """Clean up all resources."""
        for resource_id in list(self._resources.keys()):
            self.release_resource(resource_id)


# ===== ADAPTER PATTERN =====

class GitHubScriptAdapter:
    """Adapter for importing scripts from GitHub."""
    
    def __init__(self):
        self.supported_formats = ["github_url", "raw_github_url"]
    
    def can_adapt(self, source_format: str, target_format: str) -> bool:
        """Check if adapter can handle the conversion."""
        return (source_format in self.supported_formats and 
                target_format == "simexr_script")
    
    def adapt(self, source: str, source_format: str, target_format: str) -> str:
        """Convert GitHub URL to SimExR script format."""
        if not self.can_adapt(source_format, target_format):
            raise ValueError(f"Cannot adapt from {source_format} to {target_format}")
        
        if source_format == "github_url":
            # Convert GitHub URL to raw URL
            raw_url = self._github_url_to_raw(source)
        else:
            raw_url = source
        
        # Download the script content
        import requests
        response = requests.get(raw_url)
        response.raise_for_status()
        
        script_content = response.text
        
        # Adapt to SimExR format (ensure it has a simulate function)
        if "def simulate(" not in script_content:
            script_content = self._wrap_as_simulate_function(script_content)
        
        return script_content
    
    def _github_url_to_raw(self, github_url: str) -> str:
        """Convert GitHub URL to raw content URL."""
        if "github.com" in github_url and "/blob/" in github_url:
            return github_url.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")
        return github_url
    
    def _wrap_as_simulate_function(self, script_content: str) -> str:
        """Wrap script content as a simulate function if needed."""
        # This is a simple wrapper - could be more sophisticated
        return f"""
def simulate(**params):
    '''Auto-generated simulate function wrapper.'''
    # Original script content:
{script_content}
    
    # Return some default output
    return {{"status": "completed", "params": params}}
"""


# ===== FACADE PATTERN =====

class SimulationFacade:
    """Simplified interface for complex simulation operations."""
    
    def __init__(self):
        self.factory = SimulationFactory()
        self.strategy_manager = ExecutionStrategyManager()
        self.subject = SimulationSubject()
        self.command_invoker = CommandInvoker()
        self.resource_manager = ResourceManager()
        
        # Register default strategies
        self.strategy_manager.add_strategy(LocalExecutionStrategy())
        
        # Add default logging observer
        log_observer = LoggingObserver()
        self.subject.attach(log_observer)
    
    def run_simulation(self, model_id: str, parameters: Dict[str, Any], **options) -> SimulationResult:
        """Run a simulation with simplified interface."""
        request = SimulationRequest(
            model_id=model_id,
            parameters=parameters,
            execution_options=options
        )
        
        self.subject.notify_started(request)
        
        try:
            result = self.strategy_manager.execute(request)
            self.subject.notify_completed(result)
            return result
        except Exception as e:
            self.subject.notify_failed(request, e)
            raise
    
    def import_from_github(self, github_url: str, model_name: str, metadata: Dict[str, Any] = None) -> str:
        """Import a simulation model from GitHub."""
        adapter = GitHubScriptAdapter()
        
        # Adapt the GitHub script
        script_content = adapter.adapt(github_url, "github_url", "simexr_script")
        
        # Store the model
        command = StoreModelCommand(model_name, metadata or {}, script_content)
        return self.command_invoker.execute_command(command)
    
    def create_batch_configuration(self) -> SimulationConfigBuilder:
        """Create a builder for batch simulation configuration."""
        return SimulationConfigBuilder()
    
    def add_observer(self, observer: IEventListener):
        """Add an event observer."""
        self.subject.attach(observer)
    
    def cleanup(self):
        """Clean up all resources."""
        self.resource_manager.cleanup_all()


# ===== DEPENDENCY INJECTION CONTAINER =====

class DIContainer:
    """Dependency injection container."""
    
    def __init__(self):
        self._services: Dict[str, Any] = {}
        self._factories: Dict[str, Callable] = {}
        self._singletons: Dict[str, Any] = {}
    
    def register_instance(self, service_name: str, instance: Any):
        """Register a service instance."""
        self._services[service_name] = instance
    
    def register_factory(self, service_name: str, factory: Callable):
        """Register a factory function for a service."""
        self._factories[service_name] = factory
    
    def register_singleton(self, service_name: str, factory: Callable):
        """Register a singleton service."""
        self._factories[service_name] = factory
        # Mark as singleton by adding to singletons dict with None value
        if service_name not in self._singletons:
            self._singletons[service_name] = None
    
    def get(self, service_name: str) -> Any:
        """Get a service instance."""
        # Check for direct instance
        if service_name in self._services:
            return self._services[service_name]
        
        # Check for singleton
        if service_name in self._singletons:
            if self._singletons[service_name] is None:
                self._singletons[service_name] = self._factories[service_name]()
            return self._singletons[service_name]
        
        # Check for factory
        if service_name in self._factories:
            return self._factories[service_name]()
        
        raise ValueError(f"Service {service_name} not registered")
    
    def has(self, service_name: str) -> bool:
        """Check if a service is registered."""
        return (service_name in self._services or 
                service_name in self._factories or 
                service_name in self._singletons)
