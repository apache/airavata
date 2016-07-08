# simstream
A utility for user-defined remote system and simulation data monitoring.

## Dependencies
* pika >= 0.10.0 (`pip install pika`)
* A running, accessible instance of RabbitMQ server

## Installation
1. Clone this repository
2. `python setup.py install`

## Running the Example
The example runs a simple collector that records the maximum memory used by the server (MB) and a timestamp. It also generates a plot of the results.

1. Edit `example/memory_consumption.py` and `example/memory_streamer.py` with the correct RabbitMQ settings
2. From the repository root, run `python example/memory_consumption.py`
3. Open a new terminal session and run `python example/memory_streamer.py`
4. Memory usage information should now be collected in the current terminal and received in the original terminal
