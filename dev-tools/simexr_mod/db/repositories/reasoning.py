
from pathlib import Path
from typing import List
import json
from ..config.database import DatabaseConfig

class ReasoningRepository:
    def __init__(self, db_config: DatabaseConfig = None):
        self.db_config = db_config or DatabaseConfig()

    def store_report(self, model_id: str, question: str, answer: str, image_paths: List[str]) -> None:
        """
        Insert a reasoning report into the `reasoning_agent` table.
        """
        with self.db_config.get_sqlite_connection() as conn:
            conn.execute("""
                INSERT INTO reasoning_agent (model_id, question, answer, images)
                VALUES (?, ?, ?, ?)
            """, (
                model_id,
                question,
                answer,
                json.dumps(image_paths, ensure_ascii=False),
            ))