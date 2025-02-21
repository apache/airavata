#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
from __future__ import annotations
from typing import Any
import pydantic
from .runtime import Runtime
from rich.progress import Progress

class Task(pydantic.BaseModel):

  name: str
  app_id: str
  project: str = pydantic.Field(default="Default Project")
  inputs: dict[str, Any]
  runtime: Runtime
  ref: str | None = pydantic.Field(default=None)
  pid: str | None = pydantic.Field(default=None)
  agent_ref: str | None = pydantic.Field(default=None)
  workdir: str | None = pydantic.Field(default=None)
  sr_host: str | None = pydantic.Field(default=None)

  @pydantic.field_validator("runtime", mode="before")
  def set_runtime(cls, v):
    if isinstance(v, dict) and "id" in v:
      id = v.pop("id")
      args = v.pop("args", {})
      return Runtime.create(id=id, args=args)
    return v

  def __str__(self) -> str:
    return f"Task(\nname={self.name}\napp_id={self.app_id}\ninputs={self.inputs}\nruntime={self.runtime}\nref={self.ref}\nagent_ref={self.agent_ref}\nfile_path={self.sr_host}:{self.workdir}\n)"

  def launch(self, force=True) -> None:
    if not force and self.ref is not None:
      print(f"[Task] Task {self.name} has already launched: ref={self.ref}")
      return
    if self.ref is not None:
      input("[NOTE] Past runs will be overwritten! Hit Enter to continue...")
    self.ref = None
    self.agent_ref = None
    print(f"[Task] Executing {self.name} on {self.runtime}")
    self.runtime.execute(self)

  def status(self) -> tuple[str, str]:
    assert self.ref is not None
    return self.runtime.status(self)

  def ls(self) -> list[str]:
    assert self.ref is not None
    return self.runtime.ls(self)
  
  def upload(self, file: str) -> str:
    assert self.ref is not None
    from pathlib import Path
    return self.runtime.upload(Path(file), self)
  
  def download(self, file: str, local_dir: str) -> str:
    assert self.ref is not None
    from pathlib import Path
    Path(local_dir).mkdir(parents=True, exist_ok=True)
    return self.runtime.download(file, local_dir, self)
  
  def download_all(self, local_dir: str) -> list[str]:
    assert self.ref is not None
    import os
    os.makedirs(local_dir, exist_ok=True)
    fps_task = list[str]()
    files = self.ls()
    with Progress() as progress:
      pbar = progress.add_task(f"Downloading: ...", total=len(files))
      for remote_fp in self.ls():
        fp = self.runtime.download(remote_fp, local_dir, self)
        progress.update(pbar, description=f"Downloading: {remote_fp}", advance=1)
        fps_task.append(fp)
      progress.update(pbar, description=f"Downloading: DONE", refresh=True)
    return fps_task
  
  def cat(self, file: str) -> bytes:
    assert self.ref is not None
    return self.runtime.cat(file, self)

  def stop(self) -> None:
    assert self.ref is not None
    return self.runtime.signal("SIGTERM", self)
  
  def context(self, packages: list[str]) -> Any:
    def decorator(func):
      def wrapper(*args, **kwargs):
        from .scripter import scriptize
        make_script = scriptize(func)
        return self.runtime.execute_py(packages, make_script(*args, **kwargs), self)
      return wrapper
    return decorator
