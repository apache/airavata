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

from . import base, md, plan
from .runtime import list_runtimes
from .auth import login, logout, context
from .airavata import AiravataOperator
from .plan import Plan
import json


def load_plan_from_file(path: str) -> plan.Plan:
    return plan.Plan.load_json(path)

def load_plan_by_id(id: str) -> Plan:
    assert context.access_token is not None
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
    
def list_all_plans() -> list[Plan]:
    assert context.access_token is not None
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


def task_context(task: base.Task):
    def inner(func):
        # take the function into the task's location
        # and execute it there. then fetch the result
        result = func(**task.inputs)
        # and return it to the caller.
        return result

    return inner


__all__ = ["login", "logout", "list_runtimes", "md", "task_context"]
