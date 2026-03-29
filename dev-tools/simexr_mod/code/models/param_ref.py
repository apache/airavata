from dataclasses import dataclass


@dataclass
class ParamRef:
    param: str
    line: int
    col: int
    kind: str       # 'subscript' | 'get' | 'alias' | 'name'
    context: str    # one-line preview + caret
