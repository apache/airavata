from dataclasses import dataclass
from typing import List, Dict, Any


@dataclass
class ReasoningResult:
    history: List[Dict[str, Any]]
    code_map: Dict[int, str]
    answer: str
    images: List[str]
