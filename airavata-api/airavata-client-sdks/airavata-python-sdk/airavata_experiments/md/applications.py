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
        "MD-Instructions-Input": ("config_file", "uri"),
        "Coordinates-PDB-File": ("pdb_file", "uri"),
        "Protein-Structure-File_PSF": ("psf_file", "uri"),
        "FF-Parameter-Files": ("ffp_files", "uri[]"),
        "Execution_Type": ("parallelism", "str"),
        "Optional_Inputs": ("other_files", "uri[]"),
        "Number of Replicas": ("num_replicas", "str"),
    }
    obj.tasks = []
    return obj


class GROMACS(ExperimentApp):
  """
  GROMACS (GROningen MAchine for Chemical Simulations) is a molecular dynamics package
  mainly designed for simulations of proteins, lipids, and nucleic acids. It was developed
  in the Biophysical Chemistry department of University of Groningen, Netherlands.
  GROMACS is one of the fastest and most popular software packages available, and can run
  on central processing units (CPUs) and graphics processing units (GPUs).
  It is free, open-source software released under the GNU General Public License (GPL).

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="Gromacs_with_OptionalRestart")

  @classmethod
  def initialize(
      cls,
      name: str,
      pib_file: str,
      coord_file: str,
      optional_files: list[str] = [],
      environment: str = "I_MPI_FABRICS shm:ofa",
  ) -> Experiment[ExperimentApp]:
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        pib_file=pib_file,
        coord_file=coord_file,
        optional_files=optional_files,
        environment=environment,
    )
    obj.input_mapping = {
        "Portable-Input-Binary-File": ("pib_file", "uri"),
        "Coordinate-File": ("coord_file", "uri"),
        "Optional_Files": ("optional_files", "uri[]"),
        "environment": ("environment", "str"),
    }
    obj.tasks = []
    return obj


class AlphaFold2(ExperimentApp):
  """
  AlphaFold is a deep learning-based protein structure prediction method developed by
  DeepMind. It was the first protein structure prediction method to achieve competitive
  accuracy with experimental methods, and has been widely adopted by the scientific
  community for various applications. AlphaFold2 is the second version of the method,
  which was released in 2021. It builds on the success of the original AlphaFold
  by incorporating new deep learning architectures and training procedures to further
  improve accuracy and speed.

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="AlphaFold2")

  @classmethod
  def initialize(
      cls,
      name: str,
      input_seq: str,
      max_template_date: str,
      model_preset: str = "monomer",
      multimers_per_model: int = 1,
  ) -> Experiment[ExperimentApp]:
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        input_seq=input_seq,
        max_template_date=max_template_date,
        model_preset=model_preset,
        multimers_per_model=multimers_per_model,
    )
    obj.input_mapping = {
        "Input Sequence(s) File": ("input_seq", "uri"),
        "Maximum Template Date": ("max_template_date", "str"),
        "MODEL_PRESET": ("model_preset", "str"),
        "Number_Of_Multimers_Per_Model": ("multimers_per_model", "str"),
    }
    obj.tasks = []
    return obj


class AMBER(ExperimentApp):
  """
  Assisted Model Building with Energy Refinement (AMBER) is a family of force fields for
  molecular dynamics of biomolecules originally developed by Peter Kollman's group at the
  University of California, San Francisco. AMBER is also the name for the molecular
  dynamics software package that simulates these force fields.
  It consists of a set of molecular mechanical force fields for the simulation of
  biomolecules (which are in the public domain, and are used in a variety of
  simulation programs); and a package of molecular simulation programs, source code and demos.

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="Amber_pmemd_CUDA")

  @classmethod
  def initialize(
      cls,
      name: str,
      coord_file: str,
      control_file: str,
      topology_file: str,
      ref_coord_file: str = "",
  ) -> Experiment[ExperimentApp]:
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        coord_file=coord_file,
        control_file=control_file,
        topology_file=topology_file,
        ref_coord_file=ref_coord_file,
    )
    obj.input_mapping = {
        "Input_Coordinates": ("coord_file", "uri"),
        "MD_control_Input": ("control_file", "uri"),
        "Parameter-Topology-File": ("topology_file", "uri"),
        "Reference-coordinate-file": ("topology_file", "uri"),
    }
    obj.tasks = []
    return obj


class Gaussian(ExperimentApp):
  """
  Gaussian is a computational chemistry software package used for calculating molecular
  electronic structure and properties. It is widely used in the field of computational
  chemistry and is known for its accuracy and efficiency in modeling chemical systems.
  Gaussian is developed by Gaussian, Inc., and is available for various operating systems
  including Windows, macOS, and Linux.

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="Gaussian16")

  @classmethod
  def initialize(
      cls,
      name: str,
      input_file: str,
      gpu_version: str,
  ) -> Experiment[ExperimentApp]:
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        input_file=input_file,
        gpu_version=gpu_version,
    )
    obj.input_mapping = {
        "Input_File": ("input_file", "uri"),
        "GPU_Version?": ("gpu_version", "str"),
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
