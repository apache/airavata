import uuid
import importlib.util
import logging
from pathlib import Path
from typing import Callable, Any


class SimulateLoader:
    """Dynamically import simulate() from a file path under a random module name."""
    
    def __init__(self):
        self._importlib_util = importlib.util
        self.logger = logging.getLogger("SimulateLoader")
        self.logger.setLevel(logging.INFO)
    
    def import_simulate(self, script_path: Path, iu=None) -> Callable[..., Any]:
        """
        Import the simulate function from a script file.
        
        Args:
            script_path: Path to the Python script containing simulate function
            iu: importlib.util module (for dependency injection/testing)
            
        Returns:
            The simulate function from the module
            
        Raises:
            ImportError: If module cannot be loaded
            AssertionError: If simulate function is missing
        """
        self.logger.info(f"[SIMULATE_LOADER] Starting import of simulate function from {script_path}")
        
        import_util = iu if iu is not None else self._importlib_util
        name = f"simulate_{uuid.uuid4().hex}"
        self.logger.info(f"[SIMULATE_LOADER] Generated module name: {name}")
        
        self.logger.info(f"[SIMULATE_LOADER] Creating spec from file location")
        spec = import_util.spec_from_file_location(name, script_path)
        if spec is None or spec.loader is None:
            self.logger.error(f"[SIMULATE_LOADER] Cannot load {script_path}")
            raise ImportError(f"Cannot load {script_path}")
        
        self.logger.info(f"[SIMULATE_LOADER] Creating module from spec")
        mod = import_util.module_from_spec(spec)
        
        self.logger.info(f"[SIMULATE_LOADER] Executing module")
        spec.loader.exec_module(mod)  # type: ignore[attr-defined]
        
        self.logger.info(f"[SIMULATE_LOADER] Checking for simulate function")
        if not hasattr(mod, "simulate"):
            self.logger.error(f"[SIMULATE_LOADER] simulate() function missing from module")
            raise AssertionError("simulate() missing")
        
        self.logger.info(f"[SIMULATE_LOADER] Successfully imported simulate function")
        return mod.simulate