# utils/config.py
import os
import yaml
from pathlib import Path
from typing import Any, Dict


class Settings:
    def __init__(self, file_name: str = "config.yaml"):
        # Candidate directories to search (in order)
        self._roots = [
            Path.cwd(),
            Path(__file__).resolve().parent.parent,  # project root
            Path(__file__).resolve().parent,         # module folder
        ]

        self._cfg: Dict[str, Any] = {}
        self._config_path: Path | None = None
        for root in self._roots:
            p = root / file_name
            if p.is_file():
                try:
                    self._cfg = yaml.safe_load(p.read_text()) or {}
                    self._config_path = p
                    break
                except Exception:
                    print(f"⚠️  Failed to parse {p}")
        else:
            print(f"⚠️  `{file_name}` not found in {self._roots}. Falling back to env vars.")

    # ---------- helpers ----------
    @property
    def project_root(self) -> Path:
        # pick the second entry from _roots (your intended project root)
        return self._roots[1]

    def _get(self, *keys: str, default: Any = None) -> Any:
        """Safe nested lookup: _get('database','path', default=None)"""
        cur = self._cfg
        for k in keys:
            if not isinstance(cur, dict) or k not in cur:
                return default
            cur = cur[k]
        return cur

    # ---------- keys ----------
    @property
    def openai_api_key(self) -> str:
        # 1) config.yaml
        key = self._get("openai", "api_key")
        if key:
            return key
        # 2) env var
        return os.environ.get("OPENAI_API_KEY", "") or ""

    @property
    def db_path(self) -> Path:
        """
        Database path priority:
        1) config.yaml: database.path
        2) env: SIMEXR_DB_PATH
        3) default: <project_root>/mcp.db
        """
        from_env = os.environ.get("SIMEXR_DB_PATH")
        val = self._get("database", "path") or from_env
        if not val:
            val = str(self.project_root / "mcp.db")
        p = Path(val).expanduser()
        # Don't force-create here; let callers decide. Just normalize to absolute.
        return p if p.is_absolute() else p.resolve()

    @property
    def media_root(self) -> Path:
        """
        Root directory for saving figures/animations, if you want one:
        1) config.yaml: media.root
        2) env: SIMEXR_MEDIA_ROOT
        3) default: <project_root>/results_media
        """
        from_env = os.environ.get("SIMEXR_MEDIA_ROOT")
        val = self._get("media", "root") or from_env
        if not val:
            val = str(self.project_root / "results_media")
        p = Path(val).expanduser()
        return p if p.is_absolute() else p.resolve()


# a singleton you can import everywhere
settings = Settings()
