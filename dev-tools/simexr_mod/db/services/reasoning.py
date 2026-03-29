from typing import List
from pathlib import Path
import logging

from db.repositories.reasoning import ReasoningRepository

log = logging.getLogger(__name__)


class ReasoningService:
    def __init__(self, repository: ReasoningRepository = None):
        self.repository = repository or ReasoningRepository()

    def store_report(self, model_id: str, question: str, answer: str, image_paths: List[str]) -> None:
        """
        Store a reasoning report with associated images.

        Args:
            model_id: The ID of the model
            question: The question asked
            answer: The answer provided
            image_paths: List of paths to associated images
        """
        try:
            self.repository.store_report(model_id, question, answer, image_paths)
            log.info("Stored reasoning report for model %s", model_id)
        except Exception as e:
            log.error("Failed to store reasoning report for model %s: %s", model_id, str(e))
            raise