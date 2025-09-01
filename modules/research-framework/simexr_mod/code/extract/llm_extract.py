from typing import Any, Dict

from core.parser import tidy_json

from pathlib import Path
import json, re
import openai
import os

def extract_script_settings(
        script_path: str,
        llm_model: str = "gpt-5-mini",
        retries: int = 4
) -> Dict[str, Any]:
    """
    Return a flat settings dict: name -> default (float for numerics/fractions; else original).
    Uses gpt-5-mini by default. Robust to malformed LLM output.
    """
    # Set OpenAI API key from config
    try:
        from utils.config import settings
        api_key = settings.openai_api_key
        if api_key:
            openai.api_key = api_key
            print(f"OpenAI API key set in llm_extract: {api_key[:10]}...")
        else:
            print("Warning: No OpenAI API key found in config for llm_extract")
    except Exception as e:
        print(f"Warning: Could not load OpenAI API key from config in llm_extract: {e}")
    
    code = Path(script_path).read_text()

    system_prompt = r"""
        You are a precise code-analysis assistant.
        Given a Python script defining a function simulate(**params), extract:

          1) All keyword parameters that simulate accepts, with their defaults (as strings) and types.
          2) All variables used as initial conditions, with their defaults (as strings or null).
          3) All independent variables (e.g. t, time), with their defaults (as strings or null).

        Return ONLY a raw JSON object with this schema:

        {
          "parameters": {
            "param1": {"default": "<value or null>", "type": "<type or unknown>"},
            …
          },
          "initial_conditions": {
            "varA": "<value or null>",
            …
          },
          "independent_variables": {
            "varX": "<value or null>",
            …
          }
        }

        Use only double-quotes, no markdown or code fences.
        Make all the keys or values strings in the response json.
    """
    user_prompt = f"---BEGIN SCRIPT---\n{code}\n---END SCRIPT---"

    raw_payload = None
    last_cleaned = None

    for attempt in range(retries):
        resp = openai.chat.completions.create(
            model=llm_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            # temperature=0.0,
        )
        out = resp.choices[0].message.content.strip()

        out = re.sub(r"```(?:json)?", "", out, flags=re.IGNORECASE)
        out = re.sub(r"^\s*json\s*\n", "", out, flags=re.IGNORECASE | re.MULTILINE)

        cleaned = tidy_json(out)
        last_cleaned = cleaned
        try:
            raw_payload = json.loads(cleaned)
            break
        except json.JSONDecodeError:
            if attempt == retries - 1:
                raise ValueError(f"Failed to parse JSON after {retries} attempts. Last output:\n{cleaned}")

    # ── normalization helpers ──
    def _as_dict(obj) -> Dict[str, Any]:
        return obj if isinstance(obj, dict) else {}

    def _convert(val: Any) -> Any:
        if isinstance(val, (int, float)) or val is None:
            return val
        if isinstance(val, str):
            s = val.strip()
            m = re.fullmatch(r"(-?\d+(?:\.\d*)?)\s*/\s*(\d+(?:\.\d*)?)", s)
            if m:
                num, den = map(float, m.groups())
                return num / den
            if re.fullmatch(r"-?\d+", s):
                try:
                    return int(s)
                except Exception:
                    pass
            try:
                return float(s)
            except Exception:
                return val
        return val

    payload = _as_dict(raw_payload or {})
    params_obj = _as_dict(payload.get("parameters", {}))
    inits_obj = _as_dict(payload.get("initial_conditions", {}))
    indep_obj = _as_dict(payload.get("independent_variables", {}))

    settings: Dict[str, Any] = {}

    for name, info in params_obj.items():
        default = info.get("default") if isinstance(info, dict) else info
        settings[name] = _convert(default)

    for name, default in inits_obj.items():
        if isinstance(default, dict):
            default = default.get("default")
        settings[name] = _convert(default)

    for name, default in indep_obj.items():
        if isinstance(default, dict):
            default = default.get("default")
        settings[name] = _convert(default)

    print(f"Settings:\n{json.dumps(settings, indent=2, ensure_ascii=False)}")
    return settings
