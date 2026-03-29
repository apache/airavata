import subprocess
from dataclasses import dataclass
from typing import Sequence

from execute.base import BaseFormatter


@dataclass
class BlackFormatter(BaseFormatter):
    """Formats Python code via black; falls back to original code on failure."""
    black_cmd: Sequence[str] = ("black", "-q", "-")

    def format(self, code: str) -> str:
        try:
            res = subprocess.run(
                list(self.black_cmd),
                input=code,
                text=True,
                capture_output=True,
                check=True,
            )
            return res.stdout
        except Exception:
            return code