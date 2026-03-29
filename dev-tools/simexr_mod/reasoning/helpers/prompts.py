import json
from typing import List, Dict, Any


def _default_system_prompt(schema: List[str]) -> str:
    return f"""
    You are a scientific reasoning assistant.
    
    Only communicate using tool calls (no free text in assistant messages).
    At each step call exactly one tool:
    
    - python_exec(code: string) → run Python on in-memory `df` (call plt.show() to emit plots) with schema {schema}.
    - run_simulation_for_model(params: object) → run ONE simulation and append results to DB.
    - run_batch_for_model(grid: object[]) → run a small list of param dicts and append results.
    - final_answer(answer: string, values?: number[], images?: string[]) → when DONE, return the final result.
    
    Rules:
    - Always send a tool call; never write prose or raw JSON in assistant content.
    - For python_exec: provide a complete snippet that uses `df`; call plt.show() per figure.
    - Ensure integer-only sizes (e.g., N) are integers in params.
    - Keep grids modest (≤ 24).
    """.strip()

def _append_tool_message(history: List[Dict[str, Any]], call_id: str, payload: Any) -> None:
    history.append({
        "role": "tool",
        "tool_call_id": call_id,
        "content": json.dumps(payload),
    })