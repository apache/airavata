/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

include "airavata-errors.thrift"

service OrchestratorService {
    string createExperiment (1: required string userName,
                             2: required string experimentName,
                             3: optional string experimentDescription) throws (1:InvalidRequestException ire
                                                                                   2:AiravataClientException ace,
                                                                                   3:AiravataSystemException ase);

    boolean launchExperiment(1: required string experimentID)  throws (1:InvalidRequestException ire,
                                                                          2:ExperimentNotFoundException enf,
                                                                          3:AiravataClientException ace,
                                                                          4:AiravataSystemException ase);
    string terminateExperiment(1: required string experimentID) throws (1:InvalidRequestException ire,
                                                                            2:ExperimentNotFoundException enf,
                                                                            3:AiravataClientException ace,
                                                                            4:AiravataSystemException ase);

}
