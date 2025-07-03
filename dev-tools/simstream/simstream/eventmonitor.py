#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

"""
Utility for monitoring collected data.

Author: Jeff Kinnison (jkinniso@nd.edu)
"""

# TODO: Add method to add handlers
# TODO: Add method to create PikaProducer
# TODO: Add method to use PikaProducer to respond to events
# TODO: Add method to deactivate monitor


class EventCheckerNotCallableException(Exception):
    pass


class EventHandlerNotCallableException(Exception):
    pass


class EventHandlerDoesNotExistException(Exception):
    pass


class EventMonitor(object):
    """Checks data for user-defined bounds violations.

    Instance variables:
    handlers -- a dict of EventHandler objects indexed by name
    """
    def __init__(self, event_check, handlers={}):
        self._event_check = event_check
        self.handlers = handlers

    def __call__(self, val):
        if not callable(self._event_check):
            raise EventCheckerNotCallableException
        self._run_handler(self.event_check(val))

    def _run_handler(self, handler_names):
        for name in handler_names:
            if name not in self.handlers:
                raise EventHandlerDoesNotExistException
            if not callable(self.handlers[name]):
                raise EventHandlerNotCallableException
            self.handlers[name]()
