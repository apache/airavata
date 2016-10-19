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
 * Component Programming Interface definition for Apache Airavata Resource Allocation Service.
 *
*/

namespace java org.apache.airavata.allocation.manager.cpi


service AllocationManagerService {

    /**
     * @param gatewayId : For identifying the Gateway
     * @param loginUserName : username to use for login
     * @param credStoreToken : token for linking the ssh key
     * @param scratchSpace : path to filesystem scratch space
     * @return sucess/failure
     *
    **/
  bool setUserComputeResource (1: required string gatewayId,
                      2: required string loginUserName,
                      3: required string credStoreToken,
                      4: required string scratchSpace)

}