import importlib.util
import inspect
import traceback
from dataclasses import dataclass
from pathlib import Path

from execute.base import BaseTester
from execute.model.smoke_test_result import SmokeTestResult


@dataclass
class SmokeTester(BaseTester):
    """
    Imports a script module and calls `simulate(**dummy)` where
    each keyword-like parameter is set to 0.0 (preserves your behavior).
    """
    timeout_s: int = 30  # currently informational; extend to enforce timeouts if needed

    def test(self, script_path: Path) -> SmokeTestResult:
        # 1) import under a fresh module name
        spec = importlib.util.spec_from_file_location("smoketest_mod", str(script_path))
        mod = importlib.util.module_from_spec(spec)
        assert spec and spec.loader, "Invalid module spec"
        spec.loader.exec_module(mod)  # type: ignore[attr-defined]

        # 2) find simulate(...)
        if not hasattr(mod, "simulate"):
            return SmokeTestResult(False, "No `simulate` function defined")

        sig = inspect.signature(mod.simulate)
        # 3) dummy args: every KW-ish param → 0.0
        dummy = {
            name: 0.0
            for name, param in sig.parameters.items()
            if param.kind in (param.POSITIONAL_OR_KEYWORD, param.KEYWORD_ONLY)
        }

        # 4) call simulate
        try:
            out = mod.simulate(**dummy)
            if not isinstance(out, dict):
                return SmokeTestResult(False, f"`simulate` returned {type(out)}, not dict")
            return SmokeTestResult(True, "OK")
        except Exception:
            return SmokeTestResult(False, traceback.format_exc())
