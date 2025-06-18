import datetime


def convert_iso8601_to_datetime(iso8601string, microseconds=True):
    """Convert ISO8601 datetime string to a datetime instance."""
    if microseconds:
        return datetime.datetime.strptime(iso8601string, "%Y-%m-%dT%H:%M:%S.%fZ")
    else:
        return datetime.datetime.strptime(iso8601string, "%Y-%m-%dT%H:%M:%SZ")
