from typing import List, Dict, Any


def _openai_tools_spec() -> List[Dict[str, Any]]:
    """
    Static tool JSON schema advertised to the LLM. Keep names in sync with tool instances.
    """
    return [
        {
            "type": "function",
            "function": {
                "name": "python_exec",
                "description": "Execute Python against the in-memory DataFrame `df` and matplotlib.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "code": {"type": "string",
                                 "description": "Complete Python snippet that uses `df` and calls plt.show()."},
                    },
                    "required": ["code"],
                    "additionalProperties": False,
                },
            },
        },
        {
            "type": "function",
            "function": {
                "name": "run_simulation_for_model",
                "description": "Run ONE simulation for the bound model_id and append results to DB.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "params": {"type": "object", "description": "Kwargs for simulate(**params)."},
                    },
                    "required": ["params"],
                    "additionalProperties": True,
                },
            },
        },
        {
            "type": "function",
            "function": {
                "name": "run_batch_for_model",
                "description": "Run a small batch (≤ 24) of parameter dicts for the bound model_id and append results.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "grid": {
                            "type": "array",
                            "items": {"type": "object"},
                            "description": "Array of parameter dicts.",
                        },
                    },
                    "required": ["grid"],
                    "additionalProperties": False,
                },
            },
        },
        {
            "type": "function",
            "function": {
                "name": "final_answer",
                "description": "Return the final answer and optional values/images to finish.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "answer": {"type": "string"},
                        "values": {"type": "array", "items": {"type": "number"}},
                        "images": {"type": "array", "items": {"type": "string"}},
                    },
                    "required": ["answer"],
                    "additionalProperties": False,
                },
            },
        },
    ]
