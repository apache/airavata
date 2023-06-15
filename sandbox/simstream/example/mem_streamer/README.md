# SimStream Example: Memory Usage Streamer

This example collects data on the memory used by the Publisher and sends that data to the Consumer.

## Instructions

### Start the Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_consumer.py`

### Starting the Consumer
1. Open a new terminal
2. `cd path/to/simstream/examples/mem_streamer`
3. `python memory_consumer.py

The Consumer should receive the memory used by the Publisher (KB) and the time that the data was collected (s since UNIX epoch) at a 2-second interval.
