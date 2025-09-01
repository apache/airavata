from typing import List, Dict, Any

def prune_history(msgs: List[Dict[str, Any]], max_assistant_bundles: int = 2) -> List[Dict[str, Any]]:
    """Prune conversation history to keep it manageable."""
    sys = next((m for m in msgs if m["role"] == "system"), None)
    usr = next((m for m in msgs if m["role"] == "user"), None)
    bundles = []
    i = 0
    while i < len(msgs):
        m = msgs[i]
        if m.get("role") == "assistant":
            b = [m]
            j = i + 1
            while j < len(msgs) and msgs[j].get("role") == "tool":
                b.append(msgs[j])
                j += 1
            bundles.append(b)
            i = j
        else:
            i += 1
    out = []
    if sys: out.append(sys)
    if usr: out.append(usr)
    for b in bundles[-max_assistant_bundles:]:
        out.extend(b)
    return out