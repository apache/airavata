import contextlib
import io
import traceback
import textwrap
import uuid
import os
import time
from pathlib import Path
from typing import Dict, Optional, Union, Any, List, Literal

import matplotlib
from matplotlib import pyplot as plt

# Temporary imports - these functions need to be implemented or imported correctly
try:
    from core.script_utils import _capture_show, _media_dir_for, sanitize_metadata
except ImportError:
    # Mock implementations for now
    def _capture_show(images_list):
        return plt.show
    
    def _media_dir_for(model_id):
        return Path(f"media/{model_id}" if model_id else "media")
    
    def sanitize_metadata(data, media_dir, media_paths, prefix=""):
        return data

matplotlib.use("Agg")          # headless matplotlib
import pandas as pd, json

from pandas import DataFrame


# tools.py
from langchain_core.tools import BaseTool
from pydantic import BaseModel, Field, PrivateAttr


ExecMode = Literal["analysis", "simulate"]

class PythonExecArgs(BaseModel):
    code: Optional[str] = Field(default=None, description="Python source to execute.")
    mode: ExecMode = Field(default="analysis", description="'analysis' or 'simulate'")
    params: Optional[Dict[str, Any]] = Field(default=None, description="Kwargs for simulate(**params) when mode='simulate'")
    model_id: Optional[str] = Field(default=None, description="Folder key for media grouping")
    timeout_s: float = Field(default=30.0, description="Per-run timeout for simulate mode (soft check)")

class PythonExecTool(BaseTool):
    """
    Unified tool:
      - analysis mode: executes arbitrary Python against optional `df`
                       returns {ok, stdout, stderr, images}
      - simulate mode: executes code that defines `def simulate(**params)->dict`
                       returns {ok, stdout, stderr, images, outputs, media_paths}
    Backwards compatible with your previous usage if you pass only code.
    """
    name: str = Field("python_exec")
    description: str = (
        "Execute Python. In 'analysis' mode, runs code against DataFrame `df` if provided. "
        "In 'simulate' mode, runs code that defines simulate(**params)->dict and returns "
        "JSON-safe outputs with media paths. Keys: ok, stdout, stderr, images, [outputs, media_paths]."
    )
    _df: Optional[DataFrame] = PrivateAttr(default=None)

    def __init__(self, df: Optional[DataFrame] = None):
        super().__init__()  # ensure BaseModel init
        self._df = df

    # LangChain calls _run with a dict of args (function calling)
    def _run(self, args: Dict[str, Any]) -> dict:
        payload = PythonExecArgs(**args)
        if not payload.code:
            return {"ok": False, "stdout": "", "stderr": "No code provided.", "images": []}
        if payload.mode == "simulate":
            return self.run_simulation(
                code=payload.code,
                params=payload.params or {},
                model_id=payload.model_id,
                timeout_s=payload.timeout_s,
            )
        # default: analysis
        return self.run_python(code=payload.code, df=self._df)

    # -------- analysis mode (unchanged behavior) --------
    def run_python(self, code: str, df: Optional[pd.DataFrame]) -> Dict[str, Any]:
        before = {f for f in os.listdir() if f.lower().endswith(".png")}
        images: List[str] = []
        old_show = _capture_show(images)

        stdout_buf, stderr_buf = io.StringIO(), io.StringIO()
        ok = True
        g = {"plt": plt, "pd": pd, "np": __import__("numpy")}
        if df is not None:
            g["df"] = df

        try:
            with contextlib.redirect_stdout(stdout_buf), contextlib.redirect_stderr(stderr_buf):
                exec(textwrap.dedent(code), g)
        except Exception:
            stderr_buf.write(traceback.format_exc())
            ok = False
        finally:
            plt.show = old_show

        after = {f for f in os.listdir() if f.lower().endswith(".png")}
        new_images = sorted(after - before)
        # also include images saved via our plt.show hook
        for p in images:
            if p not in new_images:
                new_images.append(p)

        return {"ok": ok, "stdout": stdout_buf.getvalue(), "stderr": stderr_buf.getvalue(), "images": new_images}

    # # -------- simulate mode --------
    # def run_simulation(
    #     self,
    #     code: str,
    #     params: Dict[str, Any],
    #     model_id: Optional[str] = None,
    #     timeout_s: float = 30.0,
    # ) -> Dict[str, Any]:
    #     media_dir = _media_dir_for(model_id)
    #
    #     before = {f for f in os.listdir() if f.lower().endswith(".png")}
    #     images: List[str] = []
    #     old_show = _capture_show(images)
    #
    #     stdout_buf, stderr_buf = io.StringIO(), io.StringIO()
    #     ok, ret, err = True, None, ""
    #     g = {"plt": plt, "np": __import__("numpy")}  # simulation shouldn't need df/pd by default
    #
    #     start = time.time()
    #     try:
    #         with contextlib.redirect_stdout(stdout_buf), contextlib.redirect_stderr(stderr_buf):
    #             exec(textwrap.dedent(code), g)
    #             sim = g.get("simulate")
    #             if not callable(sim):
    #                 raise RuntimeError("No callable `simulate(**params)` found.")
    #             ret = sim(**params)
    #     except Exception:
    #         ok = False
    #         err = traceback.format_exc()
    #     finally:
    #         plt.show = old_show
    #
    #     elapsed = time.time() - start
    #     if elapsed > timeout_s:
    #         ok = False
    #         err = (err + "\n" if err else "") + f"Timeout exceeded: {elapsed:.1f}s > {timeout_s:.1f}s"
    #
    #     after = {f for f in os.listdir() if f.lower().endswith(".png")}
    #     disk_new = sorted(after - before)
    #     for f in disk_new:
    #         if f not in images:
    #             images.append(f)
    #
    #     media_paths: List[str] = []
    #     outputs = sanitize_metadata(ret, media_dir, media_paths, prefix="ret")
    #
    #     return {
    #         "ok": ok,
    #         "stdout": stdout_buf.getvalue(),
    #         "stderr": err or stderr_buf.getvalue(),
    #         "images": images,
    #         "outputs": outputs,
    #         "media_paths": media_paths,
    #     }