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

const string GFAC_CPI_VERSION = "0.16.0"

service GfacService {

  /** Query gfac server to fetch the CPI version */
  string getGFACServiceVersion(),

    /**
     * @param processId
     * @param gatewayId: The GatewayId is inferred from security context and passed onto gfac.
     * @param tokenId
     * @return sucess/failure
     *
    **/
  bool submitProcess (1: required string processId,
                      2: required string gatewayId,
                      3: required string tokenId)

    /**
     *
     * @param processId
     * @param gatewayId: The GatewayId is inferred from security context and passed onto gfac.
     * @param tokenId
     * @return sucess/failure
     *
    **/
  bool cancelProcess (1: required string processId,
                      2: required string gatewayId,
                      3: required string tokenId)
}