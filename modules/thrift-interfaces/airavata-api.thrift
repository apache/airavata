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
* Application Programming Interface definition for Apache Airavata
*/

include "airavata-errors.thrift"

namespace java org.apache.airavata.api
namespace php Airavata.API
namespace cpp airavata.api
namespace perl AiravataAPI
namespace py airavata.api
namespace js AiravataAPI

/*
* Airavata Interface Versions depend upon this Thrift Interface File. When Making changes, please edit the 
*  Version Constants according to Semantic Versioning Specification (SemVer) http://semver.org.
* 
* Note: The Airavata API version may be different from the Airavata software release versions.
*
* The Airavata API version is composed as a dot delimited string with major, minor, and patch level components.
*
*  - Major: Incremented for backward incompatible changes. An example would be changes to interfaces.
*  - Minor: Incremented for backward compatible changes. An example would be the addition of a new optional methods.
*  - Patch: Incremented for bug fixes. The patch level should be increased for every edit that doesn't result
*              in a change to major/minor version numbers.
*/
const string VERSION = "0.12.0"

service Airavata {

/*
* Apache Airavata API Service Methods. For data structures associated in the signatures, please see included thrift files
*/

  /** Query Airavata to fetch the API version */
  string GetAPIVersion(),
  
  /**
   * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
   *   but infered from the authentication header. This experiment is just a persistant place holder. The client
   *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
   *   registering the experiment in a persistant store.
   *
   * @param userName
   *   The user name of the targetted gateway end user on whos behalf the experiment is being created. 
   *     the associated gateway identity can only be infered from the security hand-shake so as to avoid
   *     authorized Airavata Clients mimicing an unauthorized request. If a gateway is not registered with 
   *     Airavata, an authorization exception is thrown.
   * 
   * @param experimentName
   *    The name of the expeiment as defined by the user. The name need not be unique as uniqueness is enforced 
   *      by the generated experiment id.
   *
   * @param experimentDescription
   *     The verbose description of the experiment. This is an optional parameter.
   *
   * @return
   *   The server-side geneated experiment GUID.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time adminstrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   * @throws AiravataSystemException 
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.    *
   *
   */
  string CreateExperiment(1: required string userName,
                          2: required string experimentName,
                          3: optional string experimentDescription)
    throws (1:InvalidRequestException ire
            2:AiravataClientException ace,
            3:AiravataSystemException ase)

  string LaunchConfiguredExperiment()
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)
            
  string ConfigureAndLaunchExperiment extends ConfigureExperiment()
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)
            
  string ConfigureExperiment()
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  string CloneExperimentConfiguration()
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  string TerminateExperiment()
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

}
