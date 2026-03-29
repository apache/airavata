# utils/config.py
import os
import yaml
import importlib
import sys
from pathlib import Path
from typing import Any, Dict


class Settings:
    def __init__(self, file_name: str = "config.yaml"):
        # Clear cached environment variables at initialization
        self._clear_cached_env_vars()
        
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
    def _clear_cached_env_vars(self):
        """Clear any cached or conflicting environment variables."""
        # List of environment variables that might conflict with our config
        env_vars_to_clear = [
            "OPENAI_API_KEY_OLD", "OPENAI_API_KEY_CACHE", "OPENAI_API_KEY_BACKUP",
            "PYTHONPATH", "PYTHONHOME", "PYTHONUNBUFFERED",
            "SIMEXR_OPENAI_API_KEY", "SIMEXR_CONFIG_CACHE"
        ]
        
        cleared_vars = []
        for var in env_vars_to_clear:
            if var in os.environ:
                old_value = os.environ.pop(var)
                cleared_vars.append(f"{var}: {old_value[:20] if old_value else 'None'}")
        
        if cleared_vars:
            print(f"🧹 Cleared cached environment variables: {', '.join(cleared_vars)}")
    
    def check_env_vars(self) -> Dict[str, Any]:
        """Check current environment variables and return a summary."""
        env_summary = {}
        
        # Check OpenAI-related environment variables
        openai_vars = {k: v for k, v in os.environ.items() if 'openai' in k.lower() or 'api_key' in k.lower()}
        if openai_vars:
            env_summary['openai_vars'] = {k: v[:20] + '...' if v and len(v) > 20 else v for k, v in openai_vars.items()}
        
        # Check Python-related environment variables
        python_vars = {k: v for k, v in os.environ.items() if 'python' in k.lower()}
        if python_vars:
            env_summary['python_vars'] = {k: v[:20] + '...' if v and len(v) > 20 else v for k, v in python_vars.items()}
        
        return env_summary
    
    def clear_specific_env_var(self, var_name: str) -> bool:
        """Clear a specific environment variable if it exists."""
        if var_name in os.environ:
            old_value = os.environ.pop(var_name)
            print(f"🗑️  Cleared {var_name}: {old_value[:20] if old_value else 'None'}")
            return True
        return False
    
    def reload_modules(self, module_names: list = None):
        """Reload specified modules to clear any cached configurations."""
        if module_names is None:
            module_names = ['openai', 'utils.config', 'utils.openai_config']
        
        reloaded = []
        for module_name in module_names:
            if module_name in sys.modules:
                try:
                    importlib.reload(sys.modules[module_name])
                    reloaded.append(module_name)
                except Exception as e:
                    print(f"⚠️  Could not reload {module_name}: {e}")
        
        if reloaded:
            print(f"🔄 Reloaded modules: {', '.join(reloaded)}")
    
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
