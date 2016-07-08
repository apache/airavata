# SimStream Example: Simulating Alanine Dipeptide

This example runs a simulation of the small molecule Alanine Dipeptide and streams logs and RMSD. RMSD is a metric for judging how similar two molecular states are for the same model.

## Instructions

### Installing OpenMM
The easiest way to install OpenMM is to use the Anaconda distribution of Python and run
`conda install -c https://conda.anaconda.org/omnia openmm`

If you do not wish to use Anaconda, install OpenMM from source by following the instructions in the [OpenMM docs](http://docs.openmm.org/7.0.0/userguide/application.html#installing-openmm "OpenMM documentation")

### Start the Logfile Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/openmm_example`
3. `python openmm_log_consumer.py`

### Start the RMSD Consumer
1. Open a terminal
2. `cd path/to/simstream/examples/openmm_example`
3. `python openmm_rmsd_consumer.py`

### Starting the Producer
1. Open a new terminal
2. `cd path/to/simstream/examples/openmm_example`
3. `python openmm_streamer.py application/sim.out application/trajectory.dcd application/input.pdb application/input.pdb`

### Starting the Simulation
1. Open a new terminal
2. `cd path/to/simstream/examples/openmm_example/application`
3. `python alanine_dipeptide.py > sim.out`

The Logfile Consumer should now be printing tagged log entries to the screen; the RMSD Consumer should be printing the calculated RMSD each time the trajectory file is written.
