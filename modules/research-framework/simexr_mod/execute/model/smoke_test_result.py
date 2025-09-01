from dataclasses import dataclass


@dataclass
class SmokeTestResult:
    ok: bool
    log: str  # "OK" or traceback/message
