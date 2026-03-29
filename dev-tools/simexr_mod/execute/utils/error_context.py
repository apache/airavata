from dataclasses import dataclass


@dataclass
class ErrorContext:
    """Creates concise error context for LLM retries."""

    def syntax(self, code: str, err: SyntaxError, around: int = 2) -> str:
        lines = code.splitlines()
        lineno = err.lineno or 0
        first = max(1, lineno - around)
        last = min(len(lines), lineno + around)
        snippet = "\n".join(
            f"{'→' if i == lineno else ' '} {i:>4}: {lines[i-1]}"
            for i in range(first, last + 1)
        )
        return (
            f"SyntaxError `{err.msg}` at line {err.lineno}\n"
            f"Context:\n{snippet}\n\n"
            "Please correct the code and return only the updated Python."
        )

    def runtime(self, trace: str, limit: int = 25) -> str:
        tb_tail = "\n".join(trace.splitlines()[-limit:]) or "<no traceback captured>"
        last = trace.strip().splitlines()[-1] if trace else "<no output>"
        return (
            f"RuntimeError `{last}`\n"
            f"Traceback (last {limit} lines):\n{tb_tail}\n\n"
            "Please fix the code and return only the updated Python."
        )
