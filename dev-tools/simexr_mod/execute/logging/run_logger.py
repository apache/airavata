import json
import logging
import logging.handlers
import sys
from pathlib import Path


class RunLogger:
    """
    Logger utilities per model directory. Writes to models/<model>/logs/runner.log
    and supports JSONL append.
    """
    @staticmethod
    def _ensure_log_dir(script_path: Path) -> Path:
        log_dir = script_path.parent / "logs"
        log_dir.mkdir(parents=True, exist_ok=True)
        return log_dir

    @staticmethod
    def get_logger(script_path: Path) -> logging.Logger:
        log_dir = RunLogger._ensure_log_dir(script_path)
        log_file = log_dir / "runner.log"
        key = f"runner::{script_path.parent.resolve()}"
        logger = logging.getLogger(key)
        if logger.handlers:
            return logger

        logger.setLevel(logging.DEBUG)

        fh = logging.handlers.RotatingFileHandler(
            log_file, maxBytes=2_000_000, backupCount=5, encoding="utf-8"
        )
        fh.setLevel(logging.DEBUG)
        fmt = logging.Formatter("%(asctime)s | %(levelname)s | %(message)s")
        fh.setFormatter(fmt)
        logger.addHandler(fh)

        ch = logging.StreamHandler(sys.stdout)
        ch.setLevel(logging.INFO)
        ch.setFormatter(fmt)
        logger.addHandler(ch)

        logger.debug("run logger initialized")
        return logger

    @staticmethod
    def append_jsonl(script_path: Path, record: dict, filename: str = "runs.jsonl") -> None:
        p = RunLogger._ensure_log_dir(script_path) / filename
        p.parent.mkdir(parents=True, exist_ok=True)
        with p.open("a", encoding="utf-8") as f:
            f.write(json.dumps(record, ensure_ascii=False) + "\n")
