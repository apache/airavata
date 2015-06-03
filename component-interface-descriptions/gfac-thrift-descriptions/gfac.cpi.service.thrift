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

/*
 * Component Programming Interface definition for Apache Airavata GFac Service.
 *
*/

namespace java org.apache.airavata.gfac.cpi

const string GFAC_CPI_VERSION = "0.13.0"

service GfacService {

  /** Query gfac server to fetch the CPI version */
  string getGFACServiceVersion(),

    /**
     * After creating the experiment Data and Task Data in the orchestrator
     * Orchestrator has to invoke this operation for each Task per experiment to run
     * the actual Job related actions.
     *
     * @param experimentID
     * @param taskID
     * @param gatewayId:
     *  The GatewayId is inferred from security context and passed onto gfac.
     * @return sucess/failure
     *
    **/
  bool submitJob (1: required string experimentId,
                  2: required string taskId
                  3: required string gatewayId,
                  4: required string tokenId)

    /**
     *
     * Terminate the running job.At this point user
     * does not have to know the job ID so in the argument
     * we do not make it to required jobID to provide.
     *
     *
     * @param experimentID
     * @param taskID
     * @return sucess/failure
     *
    **/
  bool cancelJob (1: required string experimentId,
                  2: required string taskId,
                  3: required string gatewayId,
                  4: required string tokenId)
}