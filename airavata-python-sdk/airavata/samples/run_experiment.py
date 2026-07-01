#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

"""Define and launch an experiment with the high-level ``airavata.experiments`` API.

This is the recommended way to run HPC jobs through Airavata from Python. The
experiment-orchestration logic (input staging, scheduling, status polling) lives
in ``airavata.experiments``, layered on top of the protoc-generated gRPC stubs
that ``airavata`` ships. Authentication is the browser device-code flow
(``airavata.experiments.login``).

Run it as a script after editing the runtime selection and input file paths:

    python -m airavata.samples.run_experiment
"""

import airavata.experiments as ae
from airavata.experiments.md import NAMD


def main() -> None:
    # Browser device-code login (a no-op if CS_ACCESS_TOKEN is already set).
    ae.login()

    # Discover the runtimes (a cluster + an allocation group) available to you.
    runtimes = ae.find_runtimes(cluster="example-cluster", group="Default Gateway Profile")

    # Define a NAMD molecular-dynamics experiment from its input files.
    experiment = NAMD.initialize(
        name="namd-demo",
        config_file="./inputs/equilibrate.conf",
        pdb_file="./inputs/structure.pdb",
        psf_file="./inputs/structure.psf",
        ffp_files=["./inputs/par_all27_prot_lipid.prm"],
    )

    # Schedule one run on a discovered runtime, then build, launch, and await it.
    experiment.add_run(use=runtimes, cpus=16, nodes=1, walltime=30)
    plan = experiment.plan()
    plan.launch()
    plan.wait_for_completion()
    plan.download(local_dir="./results")


if __name__ == "__main__":
    main()
