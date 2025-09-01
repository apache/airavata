import re
import os
from pathlib import Path
from typing import Any, Tuple

import openai
from code.extract.llm_extract import extract_script_settings  # assumes this is defined elsewhere


def refactor_to_single_entry(
    script_path: Path,
    entry_fn: str = "simulate",
    llm_model: str = "gpt-5-mini",
    max_attempts: int = 3
) -> Tuple[Path, Any]:
    """
    Refactors a full Python simulation script into a single function `simulate(**params)`
    which overrides all internally defined parameters and returns a dict.
    Uses an agentic retry loop to recover from malformed generations.
    """
    print(f"[LLM_REFACTOR] Starting refactor_to_single_entry for {script_path}")
    original_source = script_path.read_text().strip()
    print(f"[LLM_REFACTOR] Original source length: {len(original_source)}")

    def build_prompt(source_code: str) -> str:
        return (
            f"""
        You are a helpful **code-refactoring assistant**.
        
        Your task: Take the entire Python script below and refactor it into a single function:
        
            def {entry_fn}(**params):
        
        Requirements for the new function:
        - Inline all helper functions if needed.
        - Return **one dictionary** of results with Python built-in datatypes.
        - Override all internally defined constants/globals with values from `params` if keys exist.
        - Contain **no top-level code** and **no extra function definitions**.
        - Must behave as a self-contained black box that depends *only* on its parameters.
        - Catch common issues like indentation and variable scope errors.
        - Ensure the data types for all variable are type checked and converted incase of unexpected type inputs.
        
        If initial condition values are missing from `params`, make an intelligent guess.
        
        Return ONLY the **Python source code** for the new function (no markdown, no explanations).
        
        --- Original script ---
        ```python
        {source_code}```
        """)


    def is_valid_python(source: str) -> bool:
        try:
            compile(source, "<string>", "exec")
            return True
        except SyntaxError:
            return False

    for attempt in range(1, max_attempts + 1):
        print(f"[LLM_REFACTOR] [Attempt {attempt}] Refactoring script into `{entry_fn}(**params)`...")

        prompt = build_prompt(original_source)
        print(f"[LLM_REFACTOR] Prompt length: {len(prompt)}")
        
        print(f"[LLM_REFACTOR] Making OpenAI API call...")
        resp = openai.chat.completions.create(
            model=llm_model,
            messages=[
                {"role": "system", "content": "You are a code transformation assistant."},
                {"role": "user", "content": prompt},
            ],
            # temperature=0.0,
        )
        print(f"[LLM_REFACTOR] OpenAI API call completed")

        content = resp.choices[0].message.content.strip()

        # Clean code fences
        new_src = re.sub(r"^```python\s*", "", content)
        new_src = re.sub(r"```$", "", new_src).strip()

        if is_valid_python(new_src):
            script_path.write_text(new_src)
            print(f"[Success] Script successfully refactored and written to {script_path}")
            metadata = extract_script_settings(str(script_path))
            return script_path, metadata
        else:
            print(f"[Warning] Invalid Python generated. Retrying...")

    raise RuntimeError("Failed to refactor the script after multiple attempts.")
