from pydantic import Field
from typing import Optional, List, Dict, Any

from langchain_core.tools import BaseTool
from pydantic import BaseModel

class FinalAnswerArgs(BaseModel):
    """Schema for the final answer payload."""
    answer: str = Field(..., description="Final explanation/answer.")
    values: Optional[List[float]] = Field(
        default=None,
        description="Optional numeric values to surface with the answer.",
    )
    images: Optional[List[str]] = Field(
        default=None,
        description="Optional image filenames to attach (e.g., generated plots).",
    )


class FinalAnswerTool(BaseTool):
    """
    Pseudo-tool used by the model to TERMINATE the session.

    Behavior:
      - Simply echoes the structured payload back to the agent:
          { "answer": str, "values": list[float], "images": list[str] }
      - The agent loop is responsible for:
          * merging with any previously captured images
          * calling store_report(model_id, question, answer, images)
          * returning the final ReasoningResult

    Keep this tool side-effect free; it's a signal for the agent to stop.
    """
    name: str = "final_answer"
    description: str = "Return the final answer and optional values/images to finish the task."
    args_schema: type = FinalAnswerArgs

    def _run(
        self,
        answer: str,
        values: Optional[List[float]] = None,
        images: Optional[List[str]] = None,
    ) -> Dict[str, Any]:
        # Echo payload; agent consumes it and performs persistence/return.
        return {
            "answer": answer,
            "values": values or [],
            "images": images or [],
        }

    async def _arun(
        self,
        answer: str,
        values: Optional[List[float]] = None,
        images: Optional[List[str]] = None,
    ) -> Dict[str, Any]:
        # Async mirror if your agent uses async tooling.
        return self._run(answer=answer, values=values, images=images)
