from typing import Protocol, List, Dict, Any

from reasoning.messages.model import ModelMessage


class LLMClient(Protocol):
    def chat(self, messages: List[ModelMessage], tools: List[Dict[str, Any]]) -> ModelMessage: ...
