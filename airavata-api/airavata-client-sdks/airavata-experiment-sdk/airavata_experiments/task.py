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

from typing import Any

import pydantic

from .runtime import Runtime

class Task(pydantic.BaseModel):

  name: str
  app_id: str
  inputs: dict[str, Any]
  runtime: Runtime
  ref: str | None = pydantic.Field(default=None)
  agent_ref: str | None = pydantic.Field(default=None)

  @pydantic.field_validator("runtime", mode="before")
  def set_runtime(cls, v):
    if isinstance(v, dict) and "id" in v:
      id = v.pop("id")
      args = v.pop("args", {})
      return Runtime.create(id=id, args=args)
    return v

  def __str__(self) -> str:
    return f"Task(\nname={self.name}\napp_id={self.app_id}\ninputs={self.inputs}\nruntime={self.runtime}\n)"

  def launch(self) -> None:
    assert self.ref is None
    print(f"[Task] Executing {self.name} on {self.runtime}")
    self.runtime.execute(self)

  def status(self) -> str:
    assert self.ref is not None
    return self.runtime.status(self)

  def files(self) -> list[str]:
    assert self.ref is not None
    return self.runtime.ls(self)

  def stop(self) -> None:
    assert self.ref is not None
    return self.runtime.signal("SIGTERM", self)
