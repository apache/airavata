from typing import List, Dict, Any

from reasoning.base import BaseClient
from reasoning.messages.model import ModelMessage


class OpenAIChatClient(BaseClient):
    """
    Wrapper to keep your code insulated from SDK changes.
    """
    def __init__(self, model: str = "gpt-5-mini", temperature: float = 1.0):
        super().__init__(model=model)
        import openai  # local import to avoid hard dependency elsewhere
        self._openai = openai

    def chat(self, messages: List[ModelMessage], tools: List[Dict[str, Any]]) -> ModelMessage:
        resp = self._openai.chat.completions.create(
            model=self.model,
            messages=messages,
            tools=tools
        )
        msg = resp.choices[0].message
        out: ModelMessage = {"role": "assistant", "content": msg.content or ""}
        if getattr(msg, "tool_calls", None):
            out["tool_calls"] = [
                {
                    "id": tc.id,
                    "type": "function",
                    "function": {
                        "name": tc.function.name,
                        "arguments": tc.function.arguments or "{}",
                    },
                }
                for tc in msg.tool_calls
            ]
        return out
