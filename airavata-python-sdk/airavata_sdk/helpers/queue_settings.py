"""Queue-settings calculator registry.

Relocated verbatim (in behavior) from
``airavata_django_portal_sdk.queue_settings_calculators`` plus the
``queue_settings_calculator`` decorator from ``decorators.py``. This is a pure
in-process registry with no framework dependencies.

A gateway registers calculator functions with the
:func:`queue_settings_calculator` decorator, then invokes them by id via
:func:`calculate_queue_settings`. The registered callable receives whatever
positional/keyword arguments the caller passes through, so an existing portal
caller can keep invoking ``calculate_queue_settings(id, request, experiment_model)``
unchanged.
"""

from typing import Callable, NamedTuple

QUEUE_SETTINGS_CALCULATORS = []


class QueueSettingsCalculator(NamedTuple):
    id: str
    name: str
    func: Callable


def queue_settings_calculator(_func=None, *, id=None, name=None, **kwargs):
    """Decorator for registering queue settings calculator functions."""
    def decorator(func):
        # Register decorator
        name_ = name
        if name_ is None:
            name_ = func.__name__
        id_ = id
        if id_ is None:
            id_ = func.__module__ + ":" + func.__name__
        if exists(id_):
            raise Exception(f"Duplicate queue settings calculator id: {id_}")
        QUEUE_SETTINGS_CALCULATORS.append(QueueSettingsCalculator(id_, name_, func))
        return func
    if _func is None:
        return decorator
    else:
        return decorator(_func)


def calculate_queue_settings(calculator_id, *args, **kwargs):
    """Invoke a queue settings calculator by id.

    Any positional and keyword arguments are forwarded verbatim to the
    registered calculator function.
    """
    calcs = [calc for calc in QUEUE_SETTINGS_CALCULATORS if calc.id == calculator_id]
    if len(calcs) == 0:
        raise LookupError(f"Could not find queue settings calculator for {calculator_id}")
    calc = calcs[0]
    try:
        return calc.func(*args, **kwargs)
    except Exception as e:
        raise Exception(f"Failed to calculate queue settings for {calculator_id}") from e


def get_all():
    """Return a list of all registered queue settings calculators."""
    return QUEUE_SETTINGS_CALCULATORS.copy()


def exists(calculator_id):
    calcs = [calc for calc in QUEUE_SETTINGS_CALCULATORS if calc.id == calculator_id]
    return len(calcs) == 1


def reset_registry():
    """Reset registry, used for testing."""
    global QUEUE_SETTINGS_CALCULATORS
    QUEUE_SETTINGS_CALCULATORS = []
