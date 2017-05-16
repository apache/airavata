# SimStream Example: Logfile Streaming

This example filters log file entries by starting tag and sends them to a remote listener. The listener prints the logs it receives to terminal.

## Instructions

### Start the Publisher
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_streamer.py`

### Start the Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `python log_consumer.py`

### Write Some Logs
1. Open a terminal
2. `cd path/to/simstream/examples/logfile_checker`
3. `chmod 700 generate_logs.sh`
4. `./generate_logs.sh`

This will write logs to `test.txt`. The Publisher will continuously check for new logs, filter based on the [STATUS] and [ERROR] tags, and send the filtered results to the RabbitMQ server. The Consumer will receive the filtered log entries and print them to the terminal.
