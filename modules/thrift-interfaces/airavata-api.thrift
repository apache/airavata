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
 * Application Programming Interface definition for Apache Airavata Services.
*/

include "execution-datastructures.thrift"
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
   * @param experimentMetada
   *    The create experiment will require the basic experiment metadata like the name and description, intended user, 
   *      the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
   *      the ExperimentMetadata is a required field.
   *
   * @return
   *   The server-side geneated airavata experiment globally unique identifier.
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
   * 
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  string CreateExperiment(1: required ExperimentMetadata experimentMetadata)
    throws (1:InvalidRequestException ire
            2:AiravataClientException ace,
            3:AiravataSystemException ase)

  /**
   * Fetch previously created experiment metadata.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return experimentMetada
   *   This method will return the previously stored experiment metadata.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  ExperimentMetadata GetExperimentMetadata(1:required string airavataExperimentId)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  /**
   * Configure a previously created experiment with required inputs, scheduling and other quality of service
   *   parameters. This method only updates the experiment object within the registry. The experimet has to be launched
   *   to make it actionable by the server.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @param ExperimentConfigurationData
   *    The configuration information of the experiment with application input parameters, computational resouce scheduling
   *      information, special input output handling and additional quality of service paramaters.
   *
   * @param experimentMetada
   *    Optionally update the experiment metadata. If provided, this information will overide the metadata described during the 
   *      create experiment step.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  void ConfigureExperiment(1:required string airavataExperimentId,
                           2:required ExperimentConfigurationData experimentConfigurationData,
                           3:optional ExperimentMetadata experimentMetadata)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  /**
   * Fetch the previously configired experiment configuration information.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return
   *   This method returns the previously configured experiment configuration data.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  ExperimentConfigurationData GetExperimentConfiguration(1:required string airavataExperimentId)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  /**
   * Launch a previously created and configured experiment. Airavata Server will then strart processing the request and approriate
   *   notifications and intermediate and output data will be subsequently available for this expeirment.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @param airavataCredStoreToken:
   *   A requirement to execute experiments within Airavata is to first register the targetted remote computational account
   *     credentials with Airavata Credential Store. The administrative API (related to credential store) will return a
   *     generated token associated with the registered credentials. The client has to securily posses this token id and is
   *     required to pass it to Airavata Server for all execution requests.
   *   Note: At this point only the credential store token is required so the string is directly passed here. In future if 
   *     if more security credentials are enables, then the structure ExecutionSecurityParameters should be used.
   *   Note: This parameter is not persisted within Airavata Registry for security reasons.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  void LaunchConfiguredExperiment(1:required string airavataExperimentId
                                  2:required string airavataCredStoreToken)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)
            
  /**
   * Configure and Launch a previously created experiment with required inputs, scheduling, security and other quality of service
   *   parameters. This method also launches the experiment after it is configured. If you would like to configure only 
   *   and launch at a later time or partially configure then ConfigureExperiment should be used.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   * 
   * @param ExperimentConfigurationData
   *    The configuration information of the experiment with application input parameters, computational resouce scheduling
   *      information, special input output handling and additional quality of service paramaters.
   *
   * @param experimentMetada
   *    Optionally update the experiment metadata. If provided, this information will overide the metadata described during the 
   *      create experiment step.
   * 
   * @param airavataCredStoreToken:
   *   A requirement to execute experiments within Airavata is to first register the targetted remote computational account
   *     credentials with Airavata Credential Store. The administrative API (related to credential store) will return a
   *     generated token associated with the registered credentials. The client has to securily posses this token id and is
   *     required to pass it to Airavata Server for all execution requests.
   *   Note: At this point only the credential store token is required so the string is directly passed here. In future if 
   *     if more security credentials are enables, then the structure ExecutionSecurityParameters should be used.
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
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  string ConfigureAndLaunchExperiment (1:required string airavataExperimentId
                                       2:required ExperimentConfigurationData experimentConfigurationData,
                                       3:required string airavataCredStoreToken
                                       4:optional ExperimentMetadata experimentMetadata)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  /**
   * Clone an specified experiment with a new name. A copy of the expeirment configuration is made and is persisted with new metadata.
   *   The client has to subsequently update this configuration if needed and launch the cloned experiment. 
   *
   * @param airavataExperimentIdToBeCloned
   *    This is the experiment identifier which is to be cloned.
   *
   * @param experimentMetada
   *    Once an experiment is cloned, to disambiguate, the users are suggested to provide new meatadata. This will again require
   *      the basic experiment metadata like the name and description, intended user, the gateway identifer and if the experiment
   *      should be shared public by defualt.
   *
   * @return
   *   The server-side geneated airavata experiment globally unique identifier for the newly cloned experiment.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  string CloneExperimentConfiguration(1:required string airavataExperimentIdToBeCloned,
                                      2:required ExperimentMetadata experimentMetadata)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

  /**
   * Terminate a running experiment.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
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
   *
   * @throws AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Adminstrator will be notified to take corrective action.
   *
  */
  void TerminateExperiment(1:required string airavataExperimentId)
    throws (1:InvalidRequestException ire, 
            2:ExperimentNotFoundException enf,
            3:AiravataClientException ace,
            4:AiravataSystemException ase)

}
