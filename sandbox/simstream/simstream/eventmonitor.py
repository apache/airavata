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
