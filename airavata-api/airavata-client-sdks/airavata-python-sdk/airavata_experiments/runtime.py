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
from .auth import context
import abc
from typing import Any
from pathlib import Path

import pydantic
import requests
import uuid
import time

# from .task import Task
Task = Any

def is_terminal_state(x): return x in ["CANCELED", "COMPLETED", "FAILED"]


conn_svc_url = "api.gateway.cybershuttle.org"


class Runtime(abc.ABC, pydantic.BaseModel):

  id: str
  args: dict[str, str | int | float] = pydantic.Field(default={})

  @abc.abstractmethod
  def execute(self, task: Task) -> None: ...

  @abc.abstractmethod
  def execute_py(self, libraries: list[str], code: str, task: Task) -> None: ...

  @abc.abstractmethod
  def status(self, task: Task) -> str: ...

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
  def default():
    return Remote.default()

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
    super().__init__(id="mock")

  def execute(self, task: Task) -> None:
    import uuid
    task.agent_ref = str(uuid.uuid4())
    task.ref = str(uuid.uuid4())

  def execute_py(self, libraries: list[str], code: str, task: Task) -> None:
    pass

  def status(self, task: Task) -> str:
    import random

    self._state += random.randint(0, 5)
    if self._state > 10:
      return "COMPLETED"
    return "RUNNING"

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

  @staticmethod
  def default():
    return Mock()


class Remote(Runtime):

  def __init__(self, cluster: str, category: str, queue_name: str, node_count: int, cpu_count: int, walltime: int) -> None:
    super().__init__(id="remote", args=dict(
        cluster=cluster,
        category=category,
        queue_name=queue_name,
        node_count=node_count,
        cpu_count=cpu_count,
        walltime=walltime,
    ))

  def execute(self, task: Task) -> None:
    assert task.ref is None
    assert task.agent_ref is None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    print(f"[Remote] Experiment Created: name={task.name}")
    assert "cluster" in self.args
    task.agent_ref = str(uuid.uuid4())
    launch_state = av.launch_experiment(
        experiment_name=task.name,
        app_name=task.app_id,
        inputs={**task.inputs, "agent_id": task.agent_ref, "server_url": conn_svc_url},
        computation_resource_name=str(self.args["cluster"]),
        queue_name=str(self.args["queue_name"]),
        node_count=int(self.args["node_count"]),
        cpu_count=int(self.args["cpu_count"]),
        walltime=int(self.args["walltime"]),
    )
    task.ref = launch_state.experiment_id
    task.workdir = launch_state.experiment_dir
    task.sr_host = launch_state.sr_host
    print(f"[Remote] Experiment Launched: id={task.ref}")

  def execute_py(self, libraries: list[str], code: str, task: Task) -> None:
    print(f"* Packages: {libraries}")
    print(f"* Code:\n{code}")
    try:
      res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executepythonrequest", json={
          "libraries": libraries,
          "code": code,
          "pythonVersion": "3.10", # TODO verify
          "keepAlive": False, # TODO verify
          "parentExperimentId": "/data", # the working directory
          "agentId": task.agent_ref,
      })
      data = res.json()
      if data["error"] is not None:
        raise Exception(data["error"])
      else:
        exc_id = data["executionId"]
        while True:
          res = requests.get(f"https://{conn_svc_url}/api/v1/agent/executepythonresponse/{exc_id}")
          data = res.json()
          if data["available"]:
            response = data["responseString"]
            return print(response)
          time.sleep(1)
    except Exception as e:
      print(f"\nRemote execution failed! {e}")

  def status(self, task: Task):
    assert task.ref is not None
    assert task.agent_ref is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    status = av.get_experiment_status(task.ref)
    return status

  def signal(self, signal: str, task: Task) -> None:
    assert task.ref is not None
    assert task.agent_ref is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    av.stop_experiment(task.ref)

  def ls(self, task: Task) -> list[str]:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["ls", "/data"]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        return av.list_files(task.sr_host, task.workdir)
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"https://{conn_svc_url}/api/v1/agent/executecommandresponse/{exc_id}")
        data = res.json()
        if data["available"]:
          files = data["responseString"].split("\n")
          return files
        time.sleep(1)

  def upload(self, file: Path, task: Task) -> str:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    import os
    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["cat", os.path.join("/data", file)]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        return av.upload_files(task.sr_host, [file], task.workdir).pop()
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"https://{conn_svc_url}/api/v1/agent/executecommandresponse/{exc_id}")
        data = res.json()
        if data["available"]:
          files = data["responseString"]
          return files
        time.sleep(1)

  def download(self, file: str, local_dir: str, task: Task) -> str:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    import os
    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["cat", os.path.join("/data", file)]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        return av.download_file(task.sr_host, os.path.join(task.workdir, file), local_dir)
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"https://{conn_svc_url}/api/v1/agent/executecommandresponse/{exc_id}")
        data = res.json()
        if data["available"]:
          content = data["responseString"]
          path = Path(local_dir) / Path(file).name
          with open(path, "w") as f:
            f.write(content)
          return path.as_posix()
        time.sleep(1)

  def cat(self, file: str, task: Task) -> bytes:
    assert task.ref is not None
    assert task.agent_ref is not None
    assert task.sr_host is not None
    assert task.workdir is not None

    import os
    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["cat", os.path.join("/data", file)]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        return av.cat_file(task.sr_host, os.path.join(task.workdir, file))
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"https://{conn_svc_url}/api/v1/agent/executecommandresponse/{exc_id}")
        data = res.json()
        if data["available"]:
          content = str(data["responseString"]).encode()
          return content
        time.sleep(1)

  @staticmethod
  def default():
    return Remote(cluster="login.expanse.sdsc.edu", category="gpu", queue_name="gpu-shared", node_count=1, cpu_count=24, walltime=30)
    

def list_runtimes(
    cluster: str | None = None,
    category: str | None = None,
) -> list[Runtime]:
  all_runtimes = list[Runtime]([
    Remote(cluster="login.expanse.sdsc.edu", category="gpu", queue_name="gpu-shared", node_count=1, cpu_count=10, walltime=30),
    Remote(cluster="login.expanse.sdsc.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=10, walltime=30),
    Remote(cluster="anvil.rcac.purdue.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=24, walltime=30),
  ])
  return [*filter(lambda r: (cluster in [None, r.args["cluster"]]) and (category in [None, r.args["category"]]), all_runtimes)]