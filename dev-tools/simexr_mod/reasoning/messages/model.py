from typing import TypedDict


class ModelMessage(TypedDict, total=False):
    role: str
    content: str
    tool_calls: list  # raw passthrough of SDK fields

