import json
from typing import List, Dict, Any

def extract_code_map(history: List[Dict[str, Any]]) -> Dict[int, str]:
    """
    Scan the agent’s conversation history and pull out every python_exec
    call’s `code` snippet, keyed by the step index in `history`.
    """
    code_map: Dict[int, str] = {}
    for idx, entry in enumerate(history):
        # assistant function‐call entries look like:
        # {"role":"assistant", "function_call": {"name":"python_exec", "arguments": {"code": "..."}}, ...}
        fc = entry.get("function_call")
        if fc and isinstance(fc, dict):
            args = fc.get("arguments", {})
            args = json.loads(args)
            code = args.get("code")
            if code:
                code_map[idx] = code
    return code_map
