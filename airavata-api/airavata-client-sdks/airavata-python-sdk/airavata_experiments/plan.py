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

import json
import time
import os

import pydantic
from rich.progress import Progress
from .runtime import is_terminal_state
from .task import Task
import uuid

from .airavata import AiravataOperator
from .auth import context

class Plan(pydantic.BaseModel):

  id: str | None = pydantic.Field(default=None)
  tasks: list[Task] = []

  @pydantic.field_validator("tasks", mode="before")
  def default_tasks(cls, v):
    if isinstance(v, list):
      return [Task(**task) if isinstance(task, dict) else task for task in v]
    return v

  def __stage_prepare__(self) -> None:
    print("Preparing execution plan...")

  def __stage_confirm__(self, silent: bool) -> None:
    print("Confirming execution plan...")
    if not silent:
      while True:
        res = input("Here is the execution plan. continue? (Y/n) ")
        if res.upper() in ["N"]:
          raise Exception("Execution was aborted by user.")
        elif res.upper() in ["Y", ""]:
          break
        else:
          continue

  def __stage_launch_task__(self) -> None:
    print("Launching tasks...")
    for task in self.tasks:
      task.launch()

  def __stage_status__(self) -> list:
    statuses = []
    for task in self.tasks:
      statuses.append(task.status())
    return statuses

  def __stage_stop__(self) -> None:
    print("Stopping task(s)...")
    for task in self.tasks:
      task.stop()
    print("Task(s) stopped.")

  def __stage_fetch__(self, local_dir: str) -> list[list[str]]:
    print("Fetching results...")
    fps = list[list[str]]()
    for task in self.tasks:
      runtime = task.runtime
      ref = task.ref
      task_dir = os.path.join(local_dir, task.name)
      os.makedirs(task_dir, exist_ok=True)
      fps_task = list[str]()
      assert ref is not None
      for remote_fp in task.ls():
        fp = runtime.download(remote_fp, task_dir, task)
        fps_task.append(fp)
      fps.append(fps_task)
    print("Results fetched.")
    self.save_json(os.path.join(local_dir, "plan.json"))
    return fps

  def launch(self, silent: bool = False) -> None:
    try:
      self.__stage_prepare__()
      self.__stage_confirm__(silent)
      self.__stage_launch_task__()
      self.save()
    except Exception as e:
      print(*e.args, sep="\n")

  def status(self) -> None:
    statuses = self.__stage_status__()
    for task, status in zip(self.tasks, statuses):
      print(f"{task.name}: {status}")

  def join(self, check_every_n_mins: float = 0.1) -> None:
    n = len(self.tasks)
    try:
      with Progress() as progress:
        pbars = [progress.add_task(f"{task.name} ({i+1}/{n})", total=None) for i, task in enumerate(self.tasks)]
        completed = [False] * n
        while not all(completed):
          statuses = self.__stage_status__()
          for i, (task, status) in enumerate(zip(self.tasks, statuses)):
            pbar = pbars[i]
            progress.update(pbar, description=f"{task.name} ({i+1}/{n}): {status}")
            if is_terminal_state(status):
              completed[i] = True
              progress.update(pbar, completed=True)
          sleep_time = check_every_n_mins * 60
          time.sleep(sleep_time)
        print("Task(s) complete.")
    except KeyboardInterrupt:
      print("Interrupted by user.")

  def download(self, local_dir: str):
    assert os.path.isdir(local_dir)
    self.__stage_fetch__(local_dir)

  def stop(self) -> None:
    self.__stage_stop__()
    self.save()

  def save_json(self, filename: str) -> None:
    with open(filename, "w") as f:
      json.dump(self.model_dump(), f, indent=2)

  def save(self) -> None:
    av = AiravataOperator(context.access_token)
    az = av.__airavata_token__(av.access_token, av.default_gateway_id())
    assert az.accessToken is not None
    assert az.claimsMap is not None
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + az.accessToken,
        'X-Claims': json.dumps(az.claimsMap)
    }
    import requests
    if self.id is None:
      self.id = str(uuid.uuid4())
      response = requests.post("https://api.gateway.cybershuttle.org/api/v1/plan", headers=headers, json=self.model_dump())
      print(f"Plan saved: {self.id}")
    else:
      response = requests.put(f"https://api.gateway.cybershuttle.org/api/v1/plan/{self.id}", headers=headers, json=self.model_dump())
      print(f"Plan updated: {self.id}")

    if response.status_code == 200:
      body = response.json()
      plan = json.loads(body["data"])
      assert plan["id"] == self.id
    else:
      raise Exception(response)

def load_json(filename: str) -> Plan:
  with open(filename, "r") as f:
    model = json.load(f)
    return Plan(**model)

def load(id: str | None) -> Plan:
    assert id is not None
    av = AiravataOperator(context.access_token)
    az = av.__airavata_token__(av.access_token, av.default_gateway_id())
    assert az.accessToken is not None
    assert az.claimsMap is not None
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + az.accessToken,
        'X-Claims': json.dumps(az.claimsMap)
    }
    import requests
    response = requests.get(f"https://api.gateway.cybershuttle.org/api/v1/plan/{id}", headers=headers)

    if response.status_code == 200:
      body = response.json()
      plan = json.loads(body["data"])
      return Plan(**plan)
    else:
      raise Exception(response)
    
def query() -> list[Plan]:
    av = AiravataOperator(context.access_token)
    az = av.__airavata_token__(av.access_token, av.default_gateway_id())
    assert az.accessToken is not None
    assert az.claimsMap is not None
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + az.accessToken,
        'X-Claims': json.dumps(az.claimsMap)
    }
    import requests
    response = requests.get(f"https://api.gateway.cybershuttle.org/api/v1/plan/user", headers=headers)

    if response.status_code == 200:
      items: list = response.json()
      plans = [json.loads(item["data"]) for item in items]
      return [Plan(**plan) for plan in plans]
    else:
      raise Exception(response)
