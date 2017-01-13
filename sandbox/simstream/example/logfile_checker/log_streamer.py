from simstream import SimStream, DataReporter

import sys, json

class LogMonitor(object):
    """
    A callable class that returns unprocessed lines in an open logfile.

    Instance Variables:
    logfile -- the path to the logfile to monitor
    """

    def __init__(self, logfile):
        """
        Set up a monitor for a logfile.

        Arguments:
        logfile -- the path to the logfile to monitor
        """
        self.logfile = logfile
        self._generator = None
        self._version = sys.version_info[0]

    def __call__(self):
        """
        Get the next line from the logfile.
        """
        if not self._generator:
            self._generator = self._monitor_logfile()

        lines = []

        line = self._next()
        while line is not None:
            lines.append(line)
            line = self._next()

        return lines

    def _monitor_logfile(self):
        """
        Yield the next set of lines from the logfile.
        """
        try:
            # Make the file persistent for the lifetime of the generator
            with open(self.logfile) as f:
                f.seek(0,2) # Move to the end of the file
                while True:
                    # Get the next line or indicate the end of the file
                    line = f.readline()
                    if line:
                        yield line.strip()
                    else:
                        yield None

        except EnvironmentError as e:
            # Handle I/O exceptions in an OS-agnostic way
            print("Error: Could not open file %s: %s" % (self.logfile, e))

    def _next(self):
        """
        Python 2/3 agnostic retrieval of generator values.
        """
        return self._generator.__next__() if self._version == 3 else self._generator.next()


def get_relevant_log_lines(log_lines):
    import re
    relevant_lines = []
    pattern = r'^\[(STATUS|ERROR)\]'
    for line in log_lines:
        if re.match(pattern, line) is not None:
            relevant_lines.append(line)
    return relevant_lines


#settings = {
#    "url": "amqp://guest:guest@localhost:5672",
#    "exchange": "simstream",
#    "queue": "test",
#    "routing_key": "logfile",
#    "exchange_type": "topic"
#}

settings = {}

with open("../settings.json", 'r') as f:
    settings = json.load(f)
    settings["routing_key"] = "memory"

if __name__ == "__main__":
    logfile = sys.argv[1]
    log_reporter = DataReporter()
    log_reporter.add_collector("logger",
                               LogMonitor(logfile),
                               settings["url"],
                               settings["exchange"],
                               limit=10,
                               interval=2,
                               exchange_type=settings["exchange_type"],
                               postprocessor=get_relevant_log_lines)

    log_reporter.start_streaming("logger", settings["routing_key"])

    streamer = SimStream(config=settings, reporters={"log_reporter": log_reporter})
    streamer.setup()

    try:
        streamer.start()
    except KeyboardInterrupt:
        streamer.stop()
