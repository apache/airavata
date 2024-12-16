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

from . import base, plan
from .auth import login, logout
from .runtime import list_runtimes, Runtime

__all__ = ["login", "logout", "list_runtimes", "base", "plan"]

def display_runtimes(runtimes: list[Runtime]):
  """
  Display runtimes in a tabular format
  """
  import pandas as pd
  
  records = []
  for runtime in runtimes:
    record = dict(id=runtime.id, **runtime.args)
    records.append(record)
  
  return pd.DataFrame(records)

def display_experiments(experiments: list[base.Experiment]):
  """
  Display experiments in a tabular format
  """
  import pandas as pd
  
  records = []
  for experiment in experiments:
    record = dict(name=experiment.name, application=experiment.application.app_id, num_tasks=len(experiment.tasks))
    for k, v in experiment.inputs.items():
      record[k] = ", ".join(v) if isinstance(v, list) else str(v)
    records.append(record)
  
  return pd.DataFrame(records)

def display_plans(plans: list[plan.Plan]):
  """
  Display plans in a tabular format
  """
  import pandas as pd
  
  records = []
  for plan in plans:
    for task in plan.tasks:
      record = dict(plan_id=str(plan.id))
      for k, v in task.model_dump().items():
        record[k] = ", ".join(v) if isinstance(v, list) else str(v)
      records.append(record)
  
  return pd.DataFrame(records)

def display(arg):
  
  if isinstance(arg, list):
    if all(isinstance(x, Runtime) for x in arg):
      return display_runtimes(arg)
    if all(isinstance(x, base.Experiment) for x in arg):
      return display_experiments(arg)
    if all(isinstance(x, plan.Plan) for x in arg):
      return display_plans(arg)
  else:
    if isinstance(arg, Runtime):
      return display_runtimes([arg])
    if isinstance(arg, base.Experiment):
      return display_experiments([arg])
    if isinstance(arg, plan.Plan):
      return display_plans([arg])
  
  raise NotImplementedError(f"Cannot display object of type {type(arg)}")