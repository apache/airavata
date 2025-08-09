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
import abc
from typing import Any, Literal
from pathlib import Path

import pydantic

from airavata_auth.device_auth import AuthContext

# from .task import Task
Task = Any
States = Literal[
  # Experiment States
  'CREATED',
  'VALIDATED',
  'SCHEDULED',
  'LAUNCHED',
  'EXECUTING',
  'CANCELING',
  'CANCELED',
  'COMPLETED',
  'FAILED',
  # Job States
  'SUBMITTED',
  'QUEUED',
  'ACTIVE',
  'COMPLETE',
  'CANCELED',
  'FAILED',
  'SUSPENDED',
  'UNKNOWN',
  'NON_CRITICAL_FAIL',
]

class Runtime(abc.ABC, pydantic.BaseModel):

  id: str
  args: dict[str, str | int | float] = pydantic.Field(default={})

  @abc.abstractmethod
  def execute(self, task: Task) -> None: ...

  @abc.abstractmethod
  def execute_py(self, libraries: list[str], code: str, task: Task) -> None: ...

  @abc.abstractmethod
  def execute_cmd(self, cmd: str, task: Task) -> bytes: ...

  @abc.abstractmethod
  def status(self, task: Task) -> tuple[str, str]: ...

  @abc.abstractmethod
  def signal(self, signal: str, task: Task) -> None: ...

  @abc.abstractmethod
  def ls(self, task: Task) -> list[str]: ...

  @abc.abstractmethod
  def upload(self, file: Path, task: Task) -> str: ...

  @abc.abstractmethod
  def download(self, file: str, local_dir: str, task: Task) -> str: ...

  @abc.abstractmethod
  def cat(self, file: str, task: Task) -> bytes: ...

  def __str__(self) -> str:
    return f"{self.__class__.__name__}(args={self.args})"

  @staticmethod
  def create(id: str, args: dict[str, Any]) -> Runtime:
    if id == "mock":
      return Mock(**args)
    elif id == "remote":
      return Remote(**args)
    else:
      raise ValueError(f"Unknown runtime id: {id}")

  @staticmethod
  def Remote(**kwargs):
    return Remote(**kwargs)

  @staticmethod
  def Local(**kwargs):
    return Mock(**kwargs)


class Mock(Runtime):

  _state: int = 0

  def __init__(self) -> None:
    super().__init__(id="mock", args={})

  def execute(self, task: Task) -> None:
    import uuid
    task.agent_ref = str(uuid.uuid4())
    task.ref = str(uuid.uuid4())

  def execute_cmd(self, cmd: str, task: Task) -> bytes:
    return b""

  def execute_py(self, libraries: list[str], code: str, task: Task) -> None:
    pass

  def status(self, task: Task) -> tuple[str, str]:
    import random

    self._state += random.randint(0, 5)
    if self._state > 10:
      return "N/A", "COMPLETED"
    return "N/A", "RUNNING"

  def signal(self, signal: str, task: Task) -> None:
    pass

  def ls(self, task: Task) -> list[str]:
    return [""]

  def upload(self, file: Path, task: Task) -> str:
    return ""

  def download(self, file: str, local_dir: str, task: Task) -> str:
    return ""
  
  def cat(self, file: str, task: Task) -> bytes:
    return b""


class Remote(Runtime):

  def __init__(self, cluster: str, category: str, queue_name: str, node_count: int, cpu_count: int, walltime: int, gpu_count: int = 0, group: str = "Default") -> None:
    super().__init__(id="remote", args=dict(
        cluster=cluster,
        category=category,
        queue_name=queue_name,
        node_count=node_count,
        cpu_count=cpu_count,
        gpu_count=gpu_count,
        walltime=walltime,
        group=group,
    ))

  def execute(self, task: Task) -> None:
    assert task.ref is None
    assert task.agent_ref is None
    assert {"cluster", "group", "queue_name", "node_count", "cpu_count", "gpu_count", "walltime"}.issubset(self.args.keys())
    print(f"[Remote] Creating Experiment: name={task.name}")

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    try:
      launch_state = av.launch_experiment(
          experiment_name=task.name,
          app_name=task.app_id,
          project=task.project,
          inputs=task.inputs,
          computation_resource_name=str(self.args["cluster"]),
          queue_name=str(self.args["queue_name"]),
          node_count=int(self.args["node_count"]),
          cpu_count=int(self.args["cpu_count"]),
          walltime=int(self.args["walltime"]),
          group=str(self.args["group"]),
      )
      task.agent_ref = launch_state.agent_ref
      task.pid = launch_state.process_id
      task.ref = launch_state.experiment_id
      task.workdir = launch_state.experiment_dir
      task.sr_host = launch_state.sr_host
      print(f"[Remote] Experiment Launched: id={task.ref}")
    except Exception as e:
      print(f"[Remote] Failed to launch experiment: {repr(e)}")
      raise e
    
  def execute_cmd(self, cmd: str, task: Task) -> bytes:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.pid is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    try:
      result = av.execute_cmd(task.agent_ref, cmd)
      return result
    except Exception as e:
      print(f"[Remote] Failed to execute command: {repr(e)}")
      return b""


  def execute_py(self, libraries: list[str], code: str, task: Task) -> None:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.pid is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    result = av.execute_py(task.project, libraries, code, task.agent_ref, task.pid, task.runtime.args)
    print(result)

  def status(self, task: Task) -> tuple[str, States]:
    assert task.ref is not None
    assert task.agent_ref is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    # prioritize job state, fallback to experiment state
    job_id, job_state = av.get_task_status(task.ref)
    if job_state in [AiravataOperator.JobState.UNKNOWN, AiravataOperator.JobState.NON_CRITICAL_FAIL]:
      return job_id, av.get_experiment_status(task.ref).name
    else:
      return job_id, job_state.name

  def signal(self, signal: str, task: Task) -> None:
    assert task.ref is not None
    assert task.agent_ref is not None
    
    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    av.stop_experiment(task.ref)

  def ls(self, task: Task) -> list[str]:
    assert task.ref is not None
    assert task.pid is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    files = av.list_files(task.pid, task.agent_ref, task.sr_host, task.workdir)
    return files

  def upload(self, file: Path, task: Task) -> str:
    assert task.ref is not None
    assert task.pid is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    result = av.upload_files(task.pid, task.agent_ref, task.sr_host, [file], task.workdir).pop()
    return result

  def download(self, file: str, local_dir: str, task: Task) -> str:
    assert task.ref is not None
    assert task.pid is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    result = av.download_file(task.pid, task.agent_ref, task.sr_host, file, task.workdir, local_dir)
    return result

  def cat(self, file: str, task: Task) -> bytes:
    assert task.ref is not None
    assert task.pid is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(AuthContext.get_access_token())
    content = av.cat_file(task.pid, task.agent_ref, task.sr_host, file, task.workdir)
    return content


def find_runtimes(
    cluster: str | None = None,
    category: str | None = None,
    node_count: int | None = None,
    cpu_count: int | None = None,
    group: str | None = None,
) -> list[Runtime]:
  from .airavata import AiravataOperator
  av = AiravataOperator(AuthContext.get_access_token())
  grps = av.get_available_groups()
  grp_names = [str(x.groupResourceProfileName) for x in grps]
  if group is not None:
    assert group in grp_names, f"Group {group} was not found. Available groups: {repr(grp_names)}"
    groups = [g for g in grps if str(g.groupResourceProfileName) == group]
  else:
    groups = grps
  runtimes = []
  for g in groups:
    matched_runtimes = []
    assert g.groupResourceProfileName is not None, f"Group {g} has no name"
    r: Runtime
    for r in av.get_available_runtimes(group=g.groupResourceProfileName):
      if (node_count or 1) > int(r.args["node_count"]):
        continue
      if (cpu_count or 1) > int(r.args["cpu_count"]):
        continue
      if (cluster or r.args["cluster"]) != r.args["cluster"]:
        continue
      if (category or r.args["category"]) != r.args["category"]:
        continue
      matched_runtimes.append(r)
    runtimes.extend(matched_runtimes)
  return runtimes

def is_terminal_state(x: States) -> bool:
  return x in ["CANCELED", "COMPLETE", "COMPLETED", "FAILED"]