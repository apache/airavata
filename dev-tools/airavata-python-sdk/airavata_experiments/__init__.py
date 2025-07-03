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
from typing import Any

__all__ = ["login", "logout", "list_runtimes", "base", "plan"]


def display_runtimes(runtimes: list[Runtime]) -> None:
  """
  Display runtimes in a tabular format
  """
  import pandas as pd
  
  records = []
  for runtime in runtimes:
    record = dict(id=runtime.id, **runtime.args)
    records.append(record)
  
  d = get_display_fn()
  d(pd.DataFrame(records).set_index("id"))


def display_experiments(experiments: list[base.Experiment]) -> None:
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
  
  d = get_display_fn()
  d(pd.DataFrame(records).set_index("name"))


def display_plans(plans: list[plan.Plan]) -> None:
  """
  Display plans in a tabular format
  """

  from IPython.display import HTML
  
  html = """
  <table border='1'>
      <tr>
        <th><b>Plan Id</b></th>
        <th><b>Task Id</b></th>
        <th><b>State</b></th>
        <th><b>Name</b></th>
        <th><b>App</b></th>
        <th><b>Inputs</b></th>
        <th><b>Cluster</b></th>
        <th><b>Group</b></th>
        <th><b>Category</b></th>
        <th><b>Queue</b></th>
        <th><b>Nodes</b></th>
        <th><b>CPUs</b></th>
        <th><b>GPUs</b></th>
        <th><b>Time</b></th>
      </tr>
  """

  for plan in plans:
    for task in plan.tasks:
      html += generate_task_row(plan.id or "N/A", task)
    html += "<tr></tr>"
  
  html += "</table>"

  script = """
  <script type="text/javascript">
      document.querySelectorAll('.hover-container').forEach(item => {
          item.addEventListener('mouseover', () => item.querySelector('.hover-content').style.visibility = 'visible');
          item.addEventListener('mouseout', () => item.querySelector('.hover-content').style.visibility = 'hidden');
      });
  </script>
  """

  d = get_display_fn()
  d(HTML(html + script))


def generate_task_row(plan_id: str, task: plan.Task):
  """
  Generate a row for the task
  """

  task_id, task_state = task.status() if task.ref else ("N/A", "N/A")

  return f"""
  <tr>
      <td>{plan_id[:8]}...</td>
      <td>{task_id}</td>
      <td>{task_state}</td>
      <td>{task.name}</td>
      <td>{task.app_id}</td>
      <td>
          <div style="position:relative;" class="hover-container">
              [Hover]
              <div class="hover-content" style="visibility:hidden; position:absolute; top:20px; left:0; background:white; border:1px solid black; padding:5px; z-index:10;">
                {generate_inputs_table(task.inputs)}
              </div>
          </div>
      </td>
      <td>{task.runtime.args.get("cluster", "N/A")}</td>
      <td>{task.runtime.args.get("group", "N/A")}</td>
      <td>{task.runtime.args.get("category", "N/A")}</td>
      <td>{task.runtime.args.get("queue_name", "N/A")}</td>
      <td>{task.runtime.args.get("node_count", "N/A")}</td>
      <td>{task.runtime.args.get("cpu_count", "N/A")}</td>
      <td>{task.runtime.args.get("gpu_count", "N/A")}</td>
      <td>{task.runtime.args.get("walltime", "N/A")}</td>
  </tr>
  """


def generate_inputs_table(inputs: dict):

  html = """
  <table border='1'>
    <tr><th><b>Input</b></th><th><b>Type</b></th><th><b>Value</b></th></tr>
  """
  for k, v in inputs.items():
    html += f"""<tr><th>{k}</th><th>{v.get("type")}</th><th>{v.get("value")}</th></tr>"""
  html += "</table>"
  return html

def get_display_fn() -> Any:
  try:
    from IPython.core.getipython import get_ipython
    from IPython.display import display as d
    if get_ipython() is not None and d is not None:
      return d
    else:
      raise Exception("Not in IPython environment")
  except:
    return print


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
