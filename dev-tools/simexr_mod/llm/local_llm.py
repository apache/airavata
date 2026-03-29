import subprocess

class LocalLLM:
    """
    Thin wrapper around Ollama (or LM Studio) to run a local model.

    Parameters
    ----------
    model : str
        Ollama model tag, e.g. "deepseek-coder:6.7b-instruct".
    """

    def __init__(self, model: str = "codellama:7b-instruct"):
        self.model = model

    # ------------- PUBLIC API ------------------------------------------------
    def generate(
        self,
        prompt: str,
        system_prompt: str = "",
        temperature: float = 0.0,
        num_tokens: int | None = None,            # optional n-token limit
    ) -> str:
        """
        Call the local LLM with a prompt.

        Uses the Ollama chat command:
            /set parameter temperature <value>

        Notes
        -----
        * Works even on older Ollama builds that don’t support --temp.
        * You can still change top-p, top-k, etc. the same way.
        """
        # prepend the /set command, then optional system prompt
        header_lines = [f"/set parameter temperature {temperature}"]
        if system_prompt.strip():
            header_lines.append(system_prompt.strip())
        header = "\n\n".join(header_lines)

        full_prompt = f"{header}\n\n{prompt.strip()}"

        cmd = ["ollama", "run", self.model, full_prompt]
        if num_tokens is not None:
            cmd += ["-n", str(num_tokens), "--no-cache"]

        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode != 0:
            raise RuntimeError(f"Ollama stderr:\n{result.stderr}")
        if not result.stdout.strip():
            raise RuntimeError("Ollama returned an empty response.")
        return result.stdout.strip()
