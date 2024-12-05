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

from typing import Literal
from ..base import Experiment, ExperimentApp, GUIApp


class NAMD(ExperimentApp):
  """
  Nanoscale Molecular Dynamics (NAMD, formerly Not Another Molecular Dynamics Program)
  is a computer software for molecular dynamics simulation, written using the Charm++
  parallel programming model (not to be confused with CHARMM).
  It is noted for its parallel efficiency and is often used to simulate large systems
  (millions of atoms). It has been developed by the collaboration of the Theoretical
  and Computational Biophysics Group (TCB) and the Parallel Programming Laboratory (PPL)
  at the University of Illinois Urbana–Champaign.

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="NAMD")

  @classmethod
  def initialize(
      cls,
      name: str,
      config_file: str,
      pdb_file: str,
      psf_file: str,
      ffp_files: list[str],
      other_files: list[str] = [],
      parallelism: Literal["CPU", "GPU"] = "CPU",
      num_replicas: int = 1,
  ) -> Experiment[ExperimentApp]:
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        config_file=config_file,
        pdb_file=pdb_file,
        psf_file=psf_file,
        ffp_files=ffp_files,
        parallelism=parallelism,
        other_files=other_files,
        num_replicas=num_replicas,
    )
    obj.input_mapping = {
        "MD-Instructions-Input": "config_file",  # uri? [REQUIRED]
        "Coordinates-PDB-File": "pdb_file",  # uri? [OPTIONAL]
        "Protein-Structure-File_PSF": "psf_file",  # uri? [REQUIRED]
        "FF-Parameter-Files": "ffp_files",  # uri[]? [REQUIRED]
        "Execution_Type": "parallelism",  # "CPU" | "GPU" [REQUIRED]
        "Optional_Inputs": "other_files",  # uri[]? [OPTIONAL]
        "Number of Replicas": "num_replicas",  # integer [REQUIRED]
        # "Constraints-PDB": "pdb_file",  # uri? [OPTIONAL]
        # "Replicate": None,  # "yes"? [OPTIONAL]
        # "Continue_from_Previous_Run?": None,  # "yes"? [OPTIONAL]
        # "Previous_JobID": None,  # string? [OPTIONAL] [show if "Continue_from_Previous_Run?" == "yes"]
        # "GPU Resource Warning": None,  # string? [OPTIONAL] [show if "Continue_from_Previous_Run?" == "yes"]
        # "Restart_Replicas_List": None,  # string [OPTIONAL] [show if "Continue_from_Previous_Run?" == "yes"]
    }
    obj.tasks = []
    return obj


class VMD(GUIApp):
  """
  Visual Molecular Dynamics (VMD) is a molecular visualization and analysis program
  designed for biological systems such as proteins, nucleic acids, lipid bilayer assemblies,
  etc. It also includes tools for working with volumetric data, sequence data, and arbitrary
  graphics objects. VMD can be used to animate and analyze the trajectory of molecular dynamics
  simulations, and can interactively manipulate molecules being simulated on remote computers
  (Interactive MD).

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="vmd")

  @classmethod
  def initialize(
      cls,
      name: str,
  ) -> GUIApp:
    app = cls()
    return app
