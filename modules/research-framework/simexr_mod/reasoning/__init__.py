"""
Reasoning module for SimExR - handles AI-powered analysis and reasoning operations.

This module provides classes for:
- Reasoning agents that can analyze data and answer questions
- LLM client interfaces for different AI models
- Tools for executing Python code and simulations
- Utilities for managing conversation history and extracting results
"""

# Import core classes for external use
from .base import (
    BaseAgent,
    BaseClient,
    BaseSimulationTool,
    BaseUtility,
    LLMClientProtocol,
    ReasoningToolProtocol,
    HistoryManagerProtocol,
    CodeExtractorProtocol
)

from .agent.loop import ReasoningAgent

from .messages.llm_client import LLMClient
from .messages.model import ModelMessage
from .messages.openai_client import OpenAIChatClient

from .model.reasoning_result import ReasoningResult

from .tools.final_answer import FinalAnswerTool
from .tools.python_exec import PythonExecTool
from .tools.simulate_exec import SimulateTools

from .utils.extract_code_map import extract_code_map
from .utils.history import prune_history
from .utils.json_utils import _safe_parse
from .utils.load_results import load_results

from .helpers.chat_utils import prune_history as prune_history_helper
from .helpers.prompts import _default_system_prompt, _append_tool_message

from .config.tools import _openai_tools_spec

__all__ = [
    # Base classes
    "BaseAgent",
    "BaseClient", 
    "BaseSimulationTool",
    "BaseUtility",
    "LLMClientProtocol",
    "ReasoningToolProtocol",
    "HistoryManagerProtocol",
    "CodeExtractorProtocol",
    
    # Agent classes
    "ReasoningAgent",
    
    # Message classes
    "LLMClient",
    "ModelMessage",
    "OpenAIChatClient",
    
    # Model classes
    "ReasoningResult",
    
    # Tool classes
    "FinalAnswerTool",
    "PythonExecTool",
    "SimulateTools",
    
    # Utility functions
    "extract_code_map",
    "prune_history",
    "_safe_parse",
    "load_results",
    "prune_history_helper",
    "_default_system_prompt",
    "_append_tool_message",
    "_openai_tools_spec"
]
