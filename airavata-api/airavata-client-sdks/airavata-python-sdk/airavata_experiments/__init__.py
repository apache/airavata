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
from .auth import login, logout


def load_plan(path: str) -> plan.Plan:
    return plan.Plan.load_json(path)


def task_context(task: base.Task):
    def inner(func):
        # take the function into the task's location
        # and execute it there. then fetch the result
        result = func(**task.inputs)
        # and return it to the caller.
        return result

    return inner


__all__ = ["login", "logout", "list_runtimes", "md", "task_context"]
