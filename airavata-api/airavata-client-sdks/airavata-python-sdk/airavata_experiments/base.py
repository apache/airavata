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
  resource: Runtime = Runtime.default()
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

  def create_task(self, *allowed_runtimes: Runtime, name: str | None = None) -> None:
    """
    Create a task to run the experiment on a given runtime.
    """
    runtime = random.choice(allowed_runtimes) if len(allowed_runtimes) > 0 else self.resource
    uuid_str = str(uuid.uuid4())[:4].upper()

    self.tasks.append(
        Task(
            name=name or f"{self.name}_{uuid_str}",
            app_id=self.application.app_id,
            inputs={**self.inputs},
            runtime=runtime,
        )
    )
    print(f"Task created. ({len(self.tasks)} tasks in total)")

  def add_sweep(self, *allowed_runtimes: Runtime, **space: list) -> None:
    """
    Add a sweep to the experiment.

    """
    for values in product(space.values()):
      runtime = random.choice(allowed_runtimes) if len(allowed_runtimes) > 0 else self.resource
      uuid_str = str(uuid.uuid4())[:4].upper()

      task_specific_params = dict(zip(space.keys(), values))
      agg_inputs = {**self.inputs, **task_specific_params}
      task_inputs = {k: {"value": agg_inputs[v[0]], "type": v[1]} for k, v in self.input_mapping.items()}

      self.tasks.append(Task(
          name=f"{self.name}_{uuid_str}",
          app_id=self.application.app_id,
          inputs=task_inputs,
          runtime=runtime or self.resource,
      ))

  def plan(self, **kwargs) -> Plan:
    if len(self.tasks) == 0:
      self.create_task(self.resource)
    tasks = []
    for t in self.tasks:
      agg_inputs = {**self.inputs, **t.inputs}
      task_inputs = {k: {"value": agg_inputs[v[0]], "type": v[1]} for k, v in self.input_mapping.items()}
      tasks.append(Task(name=t.name, app_id=self.application.app_id, inputs=task_inputs, runtime=t.runtime))
    return Plan(tasks=tasks)
