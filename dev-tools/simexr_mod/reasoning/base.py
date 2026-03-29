"""
Base classes for the reasoning module to provide common interfaces and inheritance.
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any, Dict, List, Protocol, Optional, Callable
from pathlib import Path


class LLMClientProtocol(Protocol):
    """Protocol for LLM client implementations."""
    
    def chat(self, messages: List[Dict[str, Any]], tools: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Send messages to LLM and get response."""
        ...


class ReasoningToolProtocol(Protocol):
    """Protocol for reasoning tool implementations."""
    
    def _run(self, **kwargs) -> Dict[str, Any]:
        """Execute the tool with given arguments."""
        ...


@dataclass
class BaseAgent(ABC):
    """Base class for all reasoning agent implementations."""
    
    model_id: str
    max_steps: int = 20
    
    @abstractmethod
    def ask(self, question: str, stop_flag: Optional[Callable[[], bool]] = None) -> Any:
        """Process a question and return reasoning result."""
        pass


@dataclass
class BaseClient(ABC):
    """Base class for all LLM client implementations."""
    
    model: str
    temperature: float = 0.0
    
    @abstractmethod
    def chat(self, messages: List[Dict[str, Any]], tools: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Send chat request to LLM."""
        pass


@dataclass
class BaseSimulationTool(ABC):
    """Base class for simulation tool implementations."""
    
    db_path: str
    default_model_id: Optional[str] = None
    
    @abstractmethod
    def run_simulation_for_model(self, params: Dict[str, Any], model_id: Optional[str] = None) -> Dict[str, Any]:
        """Run a single simulation."""
        pass
    
    @abstractmethod
    def run_batch_for_model(self, grid: List[Dict[str, Any]], model_id: Optional[str] = None) -> List[Dict[str, Any]]:
        """Run a batch of simulations."""
        pass


@dataclass
class BaseUtility(ABC):
    """Base class for utility functions."""
    
    @abstractmethod
    def process(self, data: Any) -> Any:
        """Process data according to utility function."""
        pass


class HistoryManagerProtocol(Protocol):
    """Protocol for conversation history management."""
    
    def prune_history(self, messages: List[Dict[str, Any]], max_bundles: int = 2) -> List[Dict[str, Any]]:
        """Prune conversation history to manageable size."""
        ...


class CodeExtractorProtocol(Protocol):
    """Protocol for code extraction from conversation history."""
    
    def extract_code_map(self, history: List[Dict[str, Any]]) -> Dict[int, str]:
        """Extract code snippets from conversation history."""
        ...
