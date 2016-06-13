"""
A utility for responding to user-defined events.

Author: Jeff Kinnison (jkinniso)
"""


class EventHandler(object):
    """
    """
    def __init__(self, name, handler, handler_args=[]):
        self.name = name
        self._handler = handler
        self._handler_args

    def __call__(self):
        pass
