from dataclasses import dataclass
from typing import Optional

from langchain.agents import initialize_agent, AgentType
from langchain_core.messages import HumanMessage
from langchain_core.tools import Tool
from langchain_openai import ChatOpenAI

from execute.utils.python_utils import CodeUtils
from reasoning.tools.python_exec import PythonExecTool
from utils.config import settings


@dataclass
class FixAgent:
    """
    Wraps a LangChain agent that can execute Python and propose code fixes.
    You can swap this out for another backend without touching SimulationRefiner.
    """
    llm_name: str = "gpt-4.1"
    temperature: float = 0.0
    openai_api_key: Optional[str] = None

    def __post_init__(self) -> None:
        self._llm = ChatOpenAI(
            model_name=self.llm_name,
            temperature=self.temperature,
            openai_api_key=self.openai_api_key or settings.openai_api_key,
        )

        # Python execution tool; keep signature identical to your usage
        self._py_tool = PythonExecTool()
        self._run_tool = Tool(
            name="python_exec",
            func=lambda code: self._py_tool.run_python(code, df=None),  # df=None for smoketests
            description="Executes Python code and returns {ok, stdout, stderr, images}.",
        )

        self._agent = initialize_agent(
            tools=[self._run_tool],
            llm=self._llm,
            agent=AgentType.OPENAI_FUNCTIONS,
            verbose=False,
        )

    def propose_fix(self, error_log: str, current_src: str) -> str:
        """
        Given a failing traceback and current source, returns corrected Python code.
        """
        prompt = (
            f"The following code failed during runtime with this error:\n\n"
            f"```\n{error_log.strip()}\n```\n\n"
            "Please correct the function. Return ONLY valid Python code (no markdown, no explanations):\n\n"
            f"{current_src.strip()}"
        )
        response = self._agent.run([HumanMessage(content=prompt)]).strip()
        return CodeUtils.extract_python_code(response)
