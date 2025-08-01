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
from itertools import product
from typing import Any, Generic, TypeVar
import uuid
import random

from .plan import Plan
from .runtime import Runtime
from .task import Task


class GUIApp:

  app_id: str

  def __init__(self, app_id: str) -> None:
    self.app_id = app_id

  def open(self, runtime: Runtime, location: str) -> None:
    """
    Open the GUI application
    """
    raise NotImplementedError()

  @classmethod
  @abc.abstractmethod
  def initialize(cls, **kwargs) -> GUIApp: ...


class ExperimentApp:

  app_id: str

  def __init__(self, app_id: str) -> None:
    self.app_id = app_id

  @classmethod
  @abc.abstractmethod
  def initialize(cls, **kwargs) -> Experiment: ...


T = TypeVar("T", ExperimentApp, GUIApp)


class Experiment(Generic[T], abc.ABC):

  name: str
  application: T
  inputs: dict[str, Any]
  input_mapping: dict[str, tuple[Any, str]]
  resource: Runtime = Runtime.Local()
  tasks: list[Task] = []

  def __init__(self, name: str, application: T):
    self.name = name
    self.application = application
    self.input_mapping = {}

  def with_inputs(self, **inputs: Any) -> Experiment[T]:
    """
    Add shared inputs to the experiment
    """
    self.inputs = inputs
    return self

  def with_resource(self, resource: Runtime) -> Experiment[T]:
    self.resource = resource
    return self

  def add_run(self, use: list[Runtime], cpus: int, nodes: int, walltime: int, name: str | None = None, **extra_params) -> None:
    """
    Create a task to run the experiment on a given runtime.
    """
    runtime = random.choice(use) if len(use) > 0 else self.resource
    uuid_str = str(uuid.uuid4())[:4].upper()
    # override runtime args with given values
    runtime = runtime.model_copy()
    runtime.args["cpu_count"] = cpus
    runtime.args["node_count"] = nodes
    runtime.args["walltime"] = walltime
    # add extra inputs to task inputs
    task_inputs = {**self.inputs, **extra_params}
    # create a task with the given runtime and inputs
    self.tasks.append(
        Task(
            name=f"{name or self.name}_{uuid_str}",
            app_id=self.application.app_id,
            inputs=task_inputs,
            runtime=runtime,
        )
    )
    print(f"Task created. ({len(self.tasks)} tasks in total)")

  def add_sweep(self, use: list[Runtime], cpus: int, nodes: int, walltime: int, name: str | None = None, **space: list) -> None:
    """
    Add a sweep to the experiment.

    """
    for values in product(space.values()):
      runtime = random.choice(use) if len(use) > 0 else self.resource
      uuid_str = str(uuid.uuid4())[:4].upper()
      # override runtime args with given values
      runtime = runtime.model_copy()
      runtime.args["cpu_count"] = cpus
      runtime.args["node_count"] = nodes
      runtime.args["walltime"] = walltime
      # add sweep params to task inputs
      task_specific_params = dict(zip(space.keys(), values))
      agg_inputs = {**self.inputs, **task_specific_params}
      task_inputs = {k: {"value": agg_inputs[v[0]], "type": v[1]} for k, v in self.input_mapping.items()}
      # create a task with the given runtime and inputs
      self.tasks.append(Task(
          name=f"{name or self.name}_{uuid_str}",
          app_id=self.application.app_id,
          inputs=task_inputs,
          runtime=runtime or self.resource,
      ))

  def plan(self) -> Plan:
    assert len(self.tasks) > 0, "add_run() must be called before plan() to define runtimes and resources."
    tasks = []
    for t in self.tasks:
      agg_inputs = {**self.inputs, **t.inputs}
      task_inputs = {k: {"value": agg_inputs[v[0]], "type": v[1]} for k, v in self.input_mapping.items()}
      task = Task(name=t.name, app_id=self.application.app_id, inputs=task_inputs, runtime=t.runtime)
      # task.freeze()  # TODO upload the task-related data and freeze the task
      tasks.append(task)
    plan = Plan(tasks=tasks)
    plan.save()
    return plan
