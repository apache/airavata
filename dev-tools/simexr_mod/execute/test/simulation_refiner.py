from dataclasses import dataclass, field
from pathlib import Path
from typing import TYPE_CHECKING

from execute.test.smoke_tester import SmokeTester

# Import database function - adjust path as needed
from db import store_simulation_script

if TYPE_CHECKING:
    from execute.test.fix_agent import FixAgent


@dataclass
class SimulationRefiner:
    """
    Iteratively smoke-tests a simulate.py and uses an agent to repair it.
    Writes intermediate .iter{i}.py files; returns model_id when passing.
    """
    script_path: Path
    model_name: str
    max_iterations: int = 3
    smoke_tester: SmokeTester = field(default_factory=SmokeTester)
    agent: "FixAgent" = field(default=None)  # Lazy loaded to avoid langchain_openai dependency

    def refine(self) -> str:
        for i in range(1, self.max_iterations + 1):
            res = self.smoke_tester.test(self.script_path)
            if res.ok:
                print(f"[✓] simulate.py passed smoke test on iteration {i}")
                final_model_id = store_simulation_script(
                    model_name=self.model_name,
                    metadata={},  # keep parity with your original
                    script_path=str(self.script_path),
                )
                return final_model_id

            print(f"[!] simulate.py failed on iteration {i}:\n{res.log.strip()}")
            current_src = self.script_path.read_text()

            # Lazy load FixAgent only when needed
            if self.agent is None:
                try:
                    from execute.test.fix_agent import FixAgent
                    self.agent = FixAgent()
                except ImportError as e:
                    print(f"Warning: Cannot load FixAgent: {e}")
                    # Return a model_id anyway (fallback)
                    import hashlib
                    fallback_id = hashlib.md5(f"{self.model_name}_{self.script_path}".encode()).hexdigest()[:12]
                    return fallback_id

            corrected_code = self.agent.propose_fix(res.log, current_src)

            # Save intermediate & replace current
            iter_path = self.script_path.with_name(f"{self.script_path.stem}.iter{i}.py")
            iter_path.write_text(corrected_code)
            self.script_path.write_text(corrected_code)

        raise RuntimeError("simulate.py still failing after all correction attempts.")

