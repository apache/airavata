import re
import textwrap


class CodeUtils:
    """Small static helpers for code text processing."""

    @staticmethod
    def extract_python_code(response: str) -> str:
        """
        Given an LLM response that may contain explanation + fenced code,
        extract just the Python code (same logic you had, packaged).
        """
        m = re.search(r"```(?:python)?\s*([\s\S]+?)```", response, re.IGNORECASE)
        if m:
            return m.group(1).strip()

        idx = response.find("def simulate")
        if idx != -1:
            return response[idx:].strip()

        return response.strip()

    @staticmethod
    def dedent_if_needed(code: str) -> str:
        """
        If the first non-blank line is indented, dedent & strip leading whitespace.
        """
        first = next((l for l in code.splitlines() if l.strip()), "")
        if first.startswith((" ", "\t")):
            return textwrap.dedent(code).lstrip()
        return code