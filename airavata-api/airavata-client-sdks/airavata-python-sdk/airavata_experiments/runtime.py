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

from .auth import context
import abc
from typing import Any

import pydantic
import requests
import uuid
import time

Task = Any


conn_svc_url = "api.gateway.cybershuttle.org"


class Runtime(abc.ABC, pydantic.BaseModel):

  id: str
  args: dict[str, str | int | float] = pydantic.Field(default={})

  @abc.abstractmethod
  def execute(self, task: Task) -> None: ...

  @abc.abstractmethod
  def status(self, task: Task) -> str: ...

  @abc.abstractmethod
  def signal(self, signal: str, task: Task) -> None: ...

  @abc.abstractmethod
  def ls(self, task: Task) -> list[str]: ...

  @abc.abstractmethod
  def download(self, file: str, task: Task) -> str: ...

  def __str__(self) -> str:
    return f"{self.__class__.__name__}(args={self.args})"

  @staticmethod
  def default():
    # return Mock()
    return Remote.default()

  @staticmethod
  def create(id: str, args: dict[str, Any]) -> "Runtime":
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

  def status(self, task: Task) -> str:
    import random

    self._state += random.randint(0, 5)
    if self._state > 10:
      return "COMPLETED"
    return "RUNNING"

  def signal(self, signal: str, task: Task) -> None:
    pass

  def ls(self, task: Task) -> list[str]:
    return []

  def download(self, file: str, task: Task) -> str:
    return ""

  @staticmethod
  def default():
    return Mock()


class Remote(Runtime):

  def __init__(self, **kwargs) -> None:
    super().__init__(id="remote", args=kwargs)

  def execute(self, task: Task) -> None:
    assert context.access_token is not None
    assert task.ref is None
    assert task.agent_ref is None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    print(f"[Remote] Experiment Created: name={task.name}")
    assert "cluster" in self.args
    task.agent_ref = str(uuid.uuid4())
    task.ref = av.launch_experiment(
        experiment_name=task.name,
        app_name=task.app_id,
        computation_resource_name=str(self.args["cluster"]),
        inputs={**task.inputs, "agent_id": task.agent_ref, "server_url": conn_svc_url}
    )
    print(f"[Remote] Experiment Launched: id={task.ref}")

  def status(self, task: Task):
    assert context.access_token is not None
    assert task.ref is not None
    assert task.agent_ref is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    status = av.get_experiment_status(task.ref)
    return status

  def signal(self, signal: str, task: Task) -> None:
    assert context.access_token is not None
    assert task.ref is not None
    assert task.agent_ref is not None

    from .airavata import AiravataOperator
    av = AiravataOperator(context.access_token)
    status = av.stop_experiment(task.ref)

  def ls(self, task: Task) -> list[str]:
    assert context.access_token is not None
    assert task.ref is not None
    assert task.agent_ref is not None

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["ls", "/data"]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        print("Experiment is initializing...")
        return []
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

  def download(self, file: str, task: Task) -> str:
    assert context.access_token is not None
    assert task.ref is not None
    assert task.agent_ref is not None

    res = requests.post(f"https://{conn_svc_url}/api/v1/agent/executecommandrequest", json={
        "agentId": task.agent_ref,
        "workingDir": ".",
        "arguments": ["cat", file]
    })
    data = res.json()
    if data["error"] is not None:
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

  @staticmethod
  def default():
    return Remote(
        cluster="login.expanse.sdsc.edu",
    )


def list_runtimes(**kwargs) -> list[Runtime]:
  # TODO get list using token
  return [Remote(cluster="login.expanse.sdsc.edu"), Remote(cluster="anvil.rcac.purdue.edu")]
