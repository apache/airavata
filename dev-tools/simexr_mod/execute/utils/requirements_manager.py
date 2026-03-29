import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import List, Iterable

from execute.base import BaseManager


@dataclass
class RequirementManager(BaseManager):
    """Extracts and installs Python requirements referenced by the generated code."""
    enable_install: bool = True

    _IGNORE: frozenset = frozenset({"__future__", "typing"})

    def extract(self, script: str) -> List[str]:
        """
        - Parse `REQUIREMENTS = ["pkg1", "pkg2"]`
        - Fallback: scan `import X` / `from X import ...`
        """
        pkgs: List[str] = []

        m = re.search(r"REQUIREMENTS\s*=\s*\[(.*?)\]", script, re.S)
        if m:
            pkgs.extend(re.findall(r"[\"']([^\"']+)[\"']", m.group(1)))

        for line in script.splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            m1 = re.match(r"import\s+([\w_]+)", line)
            m2 = re.match(r"from\s+([\w_]+)", line)
            name = m1.group(1) if m1 else (m2.group(1) if m2 else None)
            if name and name not in self._IGNORE:
                pkgs.append(name)

        return sorted(set(pkgs))

    def install(self, pkgs: Iterable[str], target_dir: Path = None) -> None:
        """Install packages, optionally to a target directory."""
        if not self.enable_install:
            return
        for pkg in pkgs:
            try:
                __import__(pkg)
            except ModuleNotFoundError:
                print(f"📦 Installing '{pkg}' …")
                try:
                    cmd = [sys.executable, "-m", "pip", "install", "--upgrade", "--no-cache-dir", pkg]
                    if target_dir:
                        cmd.extend(["--target", str(target_dir)])
                    subprocess.run(
                        cmd,
                        check=True,
                        stdout=subprocess.DEVNULL,
                    )
                except subprocess.CalledProcessError as e:
                    print(f"⚠️  pip install failed for '{pkg}': {e}")

    def ensure_installed(self, pkgs: Iterable[str]) -> None:
        """Legacy method for backwards compatibility."""
        self.install(pkgs)

