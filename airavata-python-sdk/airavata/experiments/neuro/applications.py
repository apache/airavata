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

from __future__ import annotations
from typing import Optional
from ..base import Experiment, ExperimentApp, GUIApp

class SpikeInput:

  def __init__(self, data: str, **meta: str):
    self.data = data
    self.meta = meta
  
  @staticmethod
  def from_h5(uri: str, node_set: str) -> SpikeInput:
    return SpikeInput(data=uri, input_type="spikes", module="h5", node_set=node_set)

  @staticmethod
  def from_nwb(uri: str, node_set: str, trial: str) -> SpikeInput:
    return SpikeInput(data=uri, input_type="spikes", module="nwb", node_set=node_set, trial=trial)


class V1(ExperimentApp):
  """
  Run a full simulation of the point-neuron V1 network with thalamacortical (LGN) input,
  background (BKG) input, and optionally, injected (INJ) input.
  The V1 network is a simplified model of the primary visual cortex (V1) in the brain.
  It consists of excitatory and inhibitory neurons that are connected in a specific
  pattern to simulate the processing of visual information.
  The simulation can be run with different types of input to study the response of
  the network to various stimuli.

  """

  def __init__(
      self,
  ) -> None:
    super().__init__(app_id="BMTK-V1")

  @classmethod
  def initialize(
      cls,
      name: str,
      lgn_spikes: SpikeInput,
      bkg_spikes: SpikeInput,
      duration: int,
      dt: int,
      injcur_spikes: Optional[SpikeInput] = None,

  ) -> Experiment[ExperimentApp]:
    import json
    
    app = cls()
    obj = Experiment[ExperimentApp](name, app).with_inputs(
        lgn_spikes=lgn_spikes.data,
        bkg_spikes=bkg_spikes.data,
        injcur_spikes_data=injcur_spikes.data if injcur_spikes else None,
        metadata=json.dumps(
          dict(
            lgn_spikes=lgn_spikes.meta,
            bkg_spikes=bkg_spikes.meta,
            injcur_spikes=injcur_spikes.meta if injcur_spikes else None,
          )
        )
    )

    obj.input_mapping = {
        "lgn_spikes": ("lgn_spikes", "uri"),
        "bkg_spikes": ("bkg_spikes", "uri"),
        "injcur_spikes": ("injcur_spikes", "uri"),
        "metadata": ("metadata", "str"),
    }
    obj.tasks = []
    return obj
