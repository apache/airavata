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

/**
 * Application Programming Interface definition for Apache Airavata Services.
 *   this parent thrift file is contains all service interfaces. The data models are 
 *   described in respective thrift files.
*/

include "airavata_errors.thrift"
include "airavata_data_models.thrift"
include "status_models.thrift"
include "job_model.thrift"
include "experiment_model.thrift"
include "workspace_model.thrift"
include "compute_resource_model.thrift"
include "scheduling_model.thrift"
include "application_io_models.thrift"
include "application_deployment_model.thrift"
include "application_interface_model.thrift"
include "gateway_resource_profile_model.thrift"
include "workflow_data_model.thrift"
include "security_model.thrift"

namespace java org.apache.airavata.api
namespace php Airavata.API
namespace cpp apache.airavata.api
namespace perl ApacheAiravataAPI
namespace py apache.airavata.api
namespace js ApacheAiravataAPI

/**
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
 *
*/
const string AIRAVATA_API_VERSION = "0.16.0"

service Airavata {

/**
 * Apache Airavata API Service Methods. For data structures associated in the signatures, please see included thrift files
*/

  /**
   * Fetch Apache Airavata API version
  */
  string getAPIVersion(1: required security_model.AuthzToken authzToken)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  string addGateway(1: required security_model.AuthzToken authzToken, 2: required workspace_model.Gateway gateway)
         throws (1: airavata_errors.InvalidRequestException ire,
                 2: airavata_errors.AiravataClientException ace,
                 3: airavata_errors.AiravataSystemException ase,
                 4: airavata_errors.AuthorizationException ae)

  void updateGateway(1: required security_model.AuthzToken authzToken, 2: required string gatewayId, 3: required workspace_model.Gateway updatedGateway)
         throws (1: airavata_errors.InvalidRequestException ire,
                 2: airavata_errors.AiravataClientException ace,
                 3: airavata_errors.AiravataSystemException ase,
                 4: airavata_errors.AuthorizationException ae)

  workspace_model.Gateway getGateway(1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  bool deleteGateway(1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
             throws (1: airavata_errors.InvalidRequestException ire,
                     2: airavata_errors.AiravataClientException ace,
                     3: airavata_errors.AiravataSystemException ase,
                     4: airavata_errors.AuthorizationException ae)

  list<workspace_model.Gateway> getAllGateways(1: required security_model.AuthzToken authzToken)
             throws (1: airavata_errors.InvalidRequestException ire,
                     2: airavata_errors.AiravataClientException ace,
                     3: airavata_errors.AiravataSystemException ase,
                     4: airavata_errors.AuthorizationException ae)

  bool isGatewayExist(1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)


  /**
    * Airavata Adminstrative Funcationality
  **/


  /**
   * Generate and Register SSH Key Pair with Airavata Credential Store.
   *
   * @param gatewayId
   *    The identifier for the requested gateway.
   *
   * @param userName
   *    The User for which the credential should be registered. For community accounts, this user is the name of the
   *    community user name. For computational resources, this user name need not be the same user name on resoruces.
   *
   * @return airavataCredStoreToken
   *   An SSH Key pair is generated and stored in the credential store and associated with users or community account
   *   belonging to a gateway.
   *
   **/

   string generateAndRegisterSSHKeys (1: required security_model.AuthzToken authzToken,
                    2: required string gatewayId,
                    3: required string userName)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase)

   string getSSHPubKey (1: required security_model.AuthzToken authzToken,
                        2: required string airavataCredStoreToken,
                        3: required string gatewayId)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase)

   map<string, string> getAllUserSSHPubKeys (1: required security_model.AuthzToken authzToken,
                                             2: required string userName)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase)
  /**
   * Creates a Project with basic metadata.
   *    A Project is a container of experiments.
   *
   * @param gatewayId
   *    The identifier for the requested gateway.
   *
   * @param Project
   *    The Project Object described in the workspace_model
   *
  */
  string createProject (1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required workspace_model.Project project)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Project
   *
  */
  void updateProject (1: required security_model.AuthzToken authzToken,
                      2: required string projectId,
                      3: required workspace_model.Project updatedProject)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.ProjectNotFoundException pnfe,
              5: airavata_errors.AuthorizationException ae)

/**
   * Get a Project by ID
   *
  */
  workspace_model.Project getProject (1: required security_model.AuthzToken authzToken, 2: required string projectId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.ProjectNotFoundException pnfe,
                5: airavata_errors.AuthorizationException ae)

  bool deleteProject (1: required security_model.AuthzToken authzToken, 2: required string projectId)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.ProjectNotFoundException pnfe,
                  5: airavata_errors.AuthorizationException ae)

 /**
   * Get all Project by user with pagination. Results will be ordered based
   * on creation time DESC
   *
   * @param gatewayId
   *    The identifier for the requested gateway.
   * @param userName
   *    The identifier of the user
   * @param limit
   *    The amount results to be fetched
   * @param offset
   *    The starting point of the results to be fetched
 **/
  list<workspace_model.Project> getUserProjects(1: required security_model.AuthzToken authzToken,
                                                   2: required string gatewayId,
                                                   3: required string userName,
                                                   4: required i32 limit,
                                                   5: required i32 offset)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Get all Project for user by project name with pagination.Results will be ordered based
   * on creation time DESC
   *
   * @param gatewayId
   *    The identifier for the requested gateway.
   * @param userName
   *    The identifier of the user
   * @param projectName
   *    The name of the project on which the results to be fetched
   * @param limit
   *    The amount results to be fetched
   * @param offset
   *    The starting point of the results to be fetched
  */
  list<workspace_model.Project> searchProjectsByProjectName (1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required string userName,
                        4: required string projectName,
                        5: required i32 limit,
                        6: required i32 offset)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  /**
    * Search and get all Projects for user by project description with pagination. Results
    * will be ordered based on creation time DESC
    *
    * @param gatewayId
    *    The identifier for the requested gateway.
    * @param userName
    *    The identifier of the user
    * @param description
    *    The description to be matched
    * @param limit
    *    The amount results to be fetched
    * @param offset
    *    The starting point of the results to be fetched
   */
  list<workspace_model.Project> searchProjectsByProjectDesc(1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required string userName,
                        4: required string description,
                        5: required i32 limit,
                        6: required i32 offset)
            throws (1: airavata_errors.InvalidRequestException ire,
                    2: airavata_errors.AiravataClientException ace,
                    3: airavata_errors.AiravataSystemException ase,
                    4: airavata_errors.AuthorizationException ae)

  /**
    * Search Experiments by experiment name with pagination. Results will be sorted
    * based on creation time DESC
    *
    * @param gatewayId
    *       Identifier of the requested gateway
    * @param userName
    *       Username of the requested user
    * @param expName
    *       Experiment name to be matched
    * @param limit
    *       Amount of results to be fetched
    * @param offset
    *       The starting point of the results to be fetched
    */
  list<experiment_model.ExperimentSummaryModel> searchExperimentsByName(1: required security_model.AuthzToken authzToken,
                          2: required string gatewayId,
                          3: required string userName,
                          4: required string expName,
                          5: required i32 limit,
                          6: required i32 offset)
            throws (1: airavata_errors.InvalidRequestException ire,
                    2: airavata_errors.AiravataClientException ace,
                    3: airavata_errors.AiravataSystemException ase,
                    4: airavata_errors.AuthorizationException ae)

  /**
    * Search Experiments by experiment name with pagination. Results will be sorted
    * based on creation time DESC
    *
    * @param gatewayId
    *       Identifier of the requested gateway
    * @param userName
    *       Username of the requested user
    * @param description
    *       Experiment description to be matched
    * @param limit
    *       Amount of results to be fetched
    * @param offset
    *       The starting point of the results to be fetched
    */
  list<experiment_model.ExperimentSummaryModel> searchExperimentsByDesc(1: required security_model.AuthzToken authzToken,
                            2: required string gatewayId,
                            3: required string userName,
                            4: required string description,
                            5: required i32 limit,
                            6: required i32 offset)
              throws (1: airavata_errors.InvalidRequestException ire,
                      2: airavata_errors.AiravataClientException ace,
                      3: airavata_errors.AiravataSystemException ase,
                      4: airavata_errors.AuthorizationException ae)

  /**
   * Search Experiments by application id with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param gatewayId
   *       Identifier of the requested gateway
   * @param userName
   *       Username of the requested user
   * @param applicationId
   *       Application id to be matched
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
  list<experiment_model.ExperimentSummaryModel> searchExperimentsByApplication(1: required security_model.AuthzToken authzToken,
                             2: required string gatewayId,
                             3: required string userName,
                             4: required string applicationId,
                             5: required i32 limit,
                             6: required i32 offset)
              throws (1: airavata_errors.InvalidRequestException ire,
                      2: airavata_errors.AiravataClientException ace,
                      3: airavata_errors.AiravataSystemException ase,
                      4: airavata_errors.AuthorizationException ae)

  /**
   * Search Experiments by experiment status with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param gatewayId
   *       Identifier of the requested gateway
   * @param userName
   *       Username of the requested user
   * @param experimentState
   *       Experiement state to be matched
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
    list<experiment_model.ExperimentSummaryModel> searchExperimentsByStatus(1: required security_model.AuthzToken authzToken,
                            2: required string gatewayId,
                            3: required string userName,
                            4: required status_models.ExperimentState experimentState,
                            5: required i32 limit,
                            6: required i32 offset)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.AiravataClientException ace,
                        3: airavata_errors.AiravataSystemException ase,
                        4: airavata_errors.AuthorizationException ae)

  /**
   * Search Experiments by experiment creation time with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param gatewayId
   *       Identifier of the requested gateway
   * @param userName
   *       Username of the requested user
   * @param fromTime
   *       Start time of the experiments creation time
   * @param toTime
   *       End time of the  experiement creation time
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
    list<experiment_model.ExperimentSummaryModel> searchExperimentsByCreationTime(1: required security_model.AuthzToken authzToken,
                            2: required string gatewayId,
                            3: required string userName,
                            4: required i64 fromTime,
                            5: required i64 toTime,
                            6: required i32 limit,
                            7: required i32 offset)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.AiravataClientException ace,
                        3: airavata_errors.AiravataSystemException ase,
                        4: airavata_errors.AuthorizationException ae)

  /**
   * Search Experiments by using multiple filter criteria with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param gatewayId
   *       Identifier of the requested gateway
   * @param userName
   *       Username of the requested user
   * @param filters
   *       map of multiple filter criteria.
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
    list<experiment_model.ExperimentSummaryModel> searchExperiments(1: required security_model.AuthzToken authzToken,
                            2: required string gatewayId,
                            3: required string userName,
                            4: map<experiment_model.ExperimentSearchFields, string> filters,
                            5: required i32 limit,
                            6: required i32 offset)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.AiravataClientException ace,
                        3: airavata_errors.AiravataSystemException ase,
                        4: airavata_errors.AuthorizationException ae)

    /**
     * Get Experiment Statisitics for the given gateway for a specific time period
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param fromTime
     *       Starting date time
     * @param toTime
     *       Ending data time
     **/
    experiment_model.ExperimentStatistics getExperimentStatistics(1: required security_model.AuthzToken authzToken,
                            2: required string gatewayId,
                            3: required i64 fromTime,
                            4: required i64 toTime)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.AiravataClientException ace,
                        3: airavata_errors.AiravataSystemException ase,
                        4: airavata_errors.AuthorizationException ae)


  /**
   * Get Experiments within project with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param projectId
   *       Identifier of the project
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
  list<experiment_model.ExperimentModel> getExperimentsInProject(1: required security_model.AuthzToken authzToken,
                  2: required string projectId,
                  3: required i32 limit,
                  4: required i32 offset)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.ProjectNotFoundException pnfe,
                  5: airavata_errors.AuthorizationException ae)

  /**
   * Get experiments by user with pagination. Results will be sorted
   * based on creation time DESC
   *
   * @param gatewayId
   *       Identifier of the requesting gateway
   * @param userName
   *       Username of the requested user
   * @param limit
   *       Amount of results to be fetched
   * @param offset
   *       The starting point of the results to be fetched
   */
  list<experiment_model.ExperimentModel> getUserExperiments(1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required string userName,
                        4: required i32 limit,
                        5: required i32 offset)
            throws (1: airavata_errors.InvalidRequestException ire,
                    2: airavata_errors.AiravataClientException ace,
                    3: airavata_errors.AiravataSystemException ase,
                    4: airavata_errors.AuthorizationException ae)

  /**
     * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     *   but inferred from the authentication header. This experiment is just a persistent place holder. The client
     *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     *   registering the experiment in a persistent store.
     *
     * @param basicExperimentMetadata
     *    The create experiment will require the basic experiment metadata like the name and description, intended user,
     *      the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
     *      the ExperimentMetadata is a required field.
     *
     * @return
     *   The server-side generated.airavata.registry.core.experiment.globally unique identifier.
     *
     * @throws org.apache.airavata.model.error.InvalidRequestException
     *    For any incorrect forming of the request itself.
     *
     * @throws org.apache.airavata.model.error.AiravataClientException
     *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *
     *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *         gateway registration steps and retry this request.
     *
     *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *         For now this is a place holder.
     *
     *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *         is implemented, the authorization will be more substantial.
     *
     * @throws org.apache.airavata.model.error.AiravataSystemException
     *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *       rather an Airavata Administrator will be notified to take corrective action.
     *
    */

  string createExperiment(1: required security_model.AuthzToken authzToken,
                          2: required string gatewayId,
                          3: required experiment_model.ExperimentModel experiment)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)



/**
  * If the experiment is not already launched experiment can be deleted.
  *
  * @param authzToken
  * @param experiementId
  *
  * @return boolean identifier for the success or failure of the deletion operation
  *
 */
  bool deleteExperiment(1: required security_model.AuthzToken authzToken,
                          2: required string experimentId)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)


  /**
   * Fetch previously created experiment metadata.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return experimentMetada
   *   This method will return the previously stored experiment metadata.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   * 
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *      
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  experiment_model.ExperimentModel getExperiment(1: required security_model.AuthzToken authzToken,
                                                 2: required string airavataExperimentId)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.ExperimentNotFoundException enf,
            3: airavata_errors.AiravataClientException ace,
            4: airavata_errors.AiravataSystemException ase,
            5: airavata_errors.AuthorizationException ae)


  /**
   * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
   * tasks -> jobs information.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return experimentMetada
   *   This method will return the previously stored experiment metadata.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   *
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   *
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  experiment_model.ExperimentModel getDetailedExperimentTree(1: required security_model.AuthzToken authzToken,
                                                   2: required string airavataExperimentId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.ExperimentNotFoundException enf,
              3: airavata_errors.AiravataClientException ace,
              4: airavata_errors.AiravataSystemException ase,
              5: airavata_errors.AuthorizationException ae)


  /**
   * Configure a previously created experiment with required inputs, scheduling and other quality of service
   *   parameters. This method only updates the experiment object within the registry. The experiment has to be launched
   *   to make it actionable by the server.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @param experimentConfigurationData
   *    The configuration information of the experiment with application input parameters, computational resource scheduling
   *      information, special input output handling and additional quality of service parameters.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   * 
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *      
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  void updateExperiment(1: required security_model.AuthzToken authzToken,
                        2: required string airavataExperimentId,
                        3: required experiment_model.ExperimentModel experiment)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.ExperimentNotFoundException enf,
            3: airavata_errors.AiravataClientException ace,
            4: airavata_errors.AiravataSystemException ase,
            5: airavata_errors.AuthorizationException ae)

  void updateExperimentConfiguration(1: required security_model.AuthzToken authzToken,
                                     2: required string airavataExperimentId,
                                     3: required experiment_model.UserConfigurationDataModel userConfiguration)
    throws (1: airavata_errors.AuthorizationException ae)

  void updateResourceScheduleing(1: required security_model.AuthzToken authzToken,
                                 2: required string airavataExperimentId,
                                 3: required scheduling_model.ComputationalResourceSchedulingModel resourceScheduling)
    throws (1: airavata_errors.AuthorizationException ae)

    /**
     *
     * Validate experiment configuration. A true in general indicates, the experiment is ready to be launched.
     *
     * @param experimentID
     * @return sucess/failure
     *
    **/
  bool validateExperiment(1: required security_model.AuthzToken authzToken,
                          2: required string airavataExperimentId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.ExperimentNotFoundException enf,
              3: airavata_errors.AiravataClientException ace,
              4: airavata_errors.AiravataSystemException ase,
              5: airavata_errors.AuthorizationException ae)

  /**
   * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
   *   notifications and intermediate and output data will be subsequently available for this experiment.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   * 
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *      
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  void launchExperiment(1: required security_model.AuthzToken authzToken,
                        2: required string airavataExperimentId,
                        3: required string gatewayId)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.ExperimentNotFoundException enf,
            3: airavata_errors.AiravataClientException ace,
            4: airavata_errors.AiravataSystemException ase,
            5: airavata_errors.AuthorizationException ae)


   status_models.ExperimentStatus getExperimentStatus(1: required security_model.AuthzToken authzToken,
                                                      2: required string airavataExperimentId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.ExperimentNotFoundException enf,
              3: airavata_errors.AiravataClientException ace,
              4: airavata_errors.AiravataSystemException ase,
              5: airavata_errors.AuthorizationException ae)

  list<application_io_models.OutputDataObjectType> getExperimentOutputs (1: required security_model.AuthzToken authzToken,
                2: required string airavataExperimentId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.ExperimentNotFoundException enf,
              3: airavata_errors.AiravataClientException ace,
              4: airavata_errors.AiravataSystemException ase,
              5: airavata_errors.AuthorizationException ae)

   list<application_io_models.OutputDataObjectType> getIntermediateOutputs (1: required security_model.AuthzToken authzToken,
                2: required string airavataExperimentId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.ExperimentNotFoundException enf,
                3: airavata_errors.AiravataClientException ace,
                4: airavata_errors.AiravataSystemException ase,
                5: airavata_errors.AuthorizationException ae)


  map<string, status_models.JobStatus> getJobStatuses(1: required security_model.AuthzToken authzToken,
                      2: required string airavataExperimentId)
              throws (1: airavata_errors.InvalidRequestException ire,
                      2: airavata_errors.ExperimentNotFoundException enf,
                      3: airavata_errors.AiravataClientException ace,
                      4: airavata_errors.AiravataSystemException ase,
                      5: airavata_errors.AuthorizationException ae)

  list<job_model.JobModel> getJobDetails(1: required security_model.AuthzToken authzToken,
                                         2: required string airavataExperimentId)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.ExperimentNotFoundException enf,
                        3: airavata_errors.AiravataClientException ace,
                        4: airavata_errors.AiravataSystemException ase,
                        5: airavata_errors.AuthorizationException ae)


  /**
   * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
   *   The client has to subsequently update this configuration if needed and launch the cloned experiment. 
   *
   * @param newExperimentName
   *    experiment name that should be used in the cloned experiment
   *
   * @param updatedExperiment
   *    Once an experiment is cloned, to disambiguate, the users are suggested to provide new metadata. This will again require
   *      the basic experiment metadata like the name and description, intended user, the gateway identifier and if the experiment
   *      should be shared public by default.
   *
   * @return
   *   The server-side generated.airavata.registry.core.experiment.globally unique identifier for the newly cloned experiment.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   * 
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *      
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  string cloneExperiment(1: required security_model.AuthzToken authzToken,
                         2: string existingExperimentID,
                         3: string newExperimentName)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.ExperimentNotFoundException enf,
            3: airavata_errors.AiravataClientException ace,
            4: airavata_errors.AiravataSystemException ase,
            5: airavata_errors.AuthorizationException ae)

  /**
   * Terminate a running experiment.
   *
   * @param airavataExperimentId
   *    The identifier for the requested experiment. This is returned during the create experiment step.
   *
   * @return
   *   This method call does not have a return value.
   *
   * @throws org.apache.airavata.model.error.InvalidRequestException
   *    For any incorrect forming of the request itself.
   * 
   * @throws org.apache.airavata.model.error.ExperimentNotFoundException
   *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
   * 
   * @throws org.apache.airavata.model.error.AiravataClientException
   *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
   *      
   *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
   *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
   *         gateway registration steps and retry this request.
   *
   *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
   *         For now this is a place holder.
   *
   *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
   *         is implemented, the authorization will be more substantial.
   *
   * @throws org.apache.airavata.model.error.AiravataSystemException
   *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
   *       rather an Airavata Administrator will be notified to take corrective action.
   *
  */
  void terminateExperiment(1: required security_model.AuthzToken authzToken,
                           2: string airavataExperimentId,
                           3: string gatewayId)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.ExperimentNotFoundException enf,
            3: airavata_errors.AiravataClientException ace,
            4: airavata_errors.AiravataSystemException ase,
            5: airavata_errors.AuthorizationException ae)

/*
 * API definitions for App Catalog related operations
 *
*/

/*
 * Application Module is a specific computational application. Many applications, particularly scientific applications
 *  are really a suite of applications or encompass an ecosystem. For instance, Amber is referred to dozens of binaries.
 *  WRF is referred for an ecosystem of applications. In this context, we refer to module as a single binary.
 *
 * Note: A module has to be defined before a deployment can be registered.
 *
*/

  /**
   * Register a Application Module.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return appModuleId
   *   Returns a server-side generated airavata appModule globally unique identifier.
   *
  */
  string registerApplicationModule(1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required application_deployment_model.ApplicationModule applicationModule)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a Application Module.
   *
   * @param appModuleId
   *   The identifier for the requested application module
   *
   * @return applicationModule
   *   Returns a application Module Object.
   *
  */
  application_deployment_model.ApplicationModule getApplicationModule(1: required security_model.AuthzToken authzToken,
                2: required string appModuleId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Application Module.
   *
   * @param appModuleId
   *   The identifier for the requested application module to be updated.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateApplicationModule(1: required security_model.AuthzToken authzToken,
            2: required string appModuleId,
            3: required application_deployment_model.ApplicationModule applicationModule)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)


  list<application_deployment_model.ApplicationModule> getAllAppModules (1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a Application Module.
   *
   * @param appModuleId
   *   The identifier for the requested application module to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteApplicationModule(1: required security_model.AuthzToken authzToken,
                               2: required string appModuleId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

/*
 * Application Deployment registers a deployment of a application module on a compute resource
 *
*/

  /**
   * Register a Application Deployment.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return appDeploymentId
   *   Returns a server-side generated airavata appDeployment globally unique identifier.
   *
  */
  string registerApplicationDeployment(1: required security_model.AuthzToken authzToken,
                2: required string gatewayId,
                3: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a Application Deployment.
   *
   * @param appDeploymentId
   *   The identifier for the requested application module
   *
   * @return applicationDeployment
   *   Returns a application Deployment Object.
   *
  */
  application_deployment_model.ApplicationDeploymentDescription getApplicationDeployment(1: required security_model.AuthzToken authzToken,
                2: required string appDeploymentId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Application Deployment.
   *
   * @param appDeploymentId
   *   The identifier for the requested application deployment to be updated.
   *
   * @param appDeployment
   *    Application Deployment Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateApplicationDeployment(1: required security_model.AuthzToken authzToken,
            2: required string appDeploymentId,
            3: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a Application deployment.
   *
   * @param appDeploymentId
   *   The identifier for the requested application deployment to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteApplicationDeployment(1: required security_model.AuthzToken authzToken,
                    2: required string appDeploymentId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch all Application Deployment Descriptions.
   *
   * @return list<applicationDeployment.
   *   Returns the list of all application Deployment Objects.
   *
  */
  list<application_deployment_model.ApplicationDeploymentDescription> getAllApplicationDeployments(1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a list of Deployed Compute Hosts.
   *
   * @param appModuleId
   *   The identifier for the requested application module
   *
   * @return list<string>
   *   Returns a list of Deployed Resources.
   *
  */
  list<string> getAppModuleDeployedResources(1: required security_model.AuthzToken authzToken, 2: required string appModuleId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

/*
 * Application Interface
 *
*/

  /**
   * Register a Application Interface.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return appInterfaceId
   *   Returns a server-side generated airavata application interface globally unique identifier.
   *
  */
  string registerApplicationInterface(1: required security_model.AuthzToken authzToken, 2: required string gatewayId,
                3: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a Application Interface.
   *
   * @param appInterfaceId
   *   The identifier for the requested application module
   *
   * @return applicationInterface
   *   Returns a application Interface Object.
   *
   *
  */
  application_interface_model.ApplicationInterfaceDescription getApplicationInterface(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Application Interface.
   *
   * @param appInterfaceId
   *   The identifier for the requested application deployment to be updated.
   *
   * @param appInterface
   *    Application Interface Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
   *
  */
  bool updateApplicationInterface(1: required security_model.AuthzToken authzToken,
            2: required string appInterfaceId,
            3: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a Application Interface.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
   *
  */
  bool deleteApplicationInterface(1: required security_model.AuthzToken authzToken, 2: required string appInterfaceId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch name and id of  Application Interface documents.
   *
   *
   * @return map<applicationId, applicationInterfaceNames>
   *   Returns a list of application interfaces with corresponsing id's
   *
  */
  map<string, string> getAllApplicationInterfaceNames (1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch all Application Interface documents.
   *
   *
   * @return map<applicationId, applicationInterfaceNames>
   *   Returns a list of application interfaces documents
   *
  */
  list<application_interface_model.ApplicationInterfaceDescription> getAllApplicationInterfaces (1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch the list of Application Inputs.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface
   *
   * @return list<application_interface_model.InputDataObjectType>
   *   Returns a list of application inputs.
   *
  */
  list<application_io_models.InputDataObjectType> getApplicationInputs(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch the list of Application Outputs.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface
   *
   * @return list<application_interface_model.OutputDataObjectType>
   *   Returns a list of application outputs.
   *
  */
  list<application_io_models.OutputDataObjectType> getApplicationOutputs(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a list of all deployed Compute Hosts for a given application interfaces.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface
   *
   * @return map<computeResourceId, computeResourceName>
   *   A map of registered compute resource id's and their corresponding hostnames.
   *    Deployments of each modules listed within the interfaces will be listed.
   *
  */
  map<string, string> getAvailableAppInterfaceComputeResources(1: required security_model.AuthzToken authzToken, 2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

/*
 * Compute Resource
 *
*/

  /**
   * Register a Compute Resource.
   *
   * @param computeResourceDescription
   *    Compute Resource Object created from the datamodel.
   *
   * @return computeResourceId
   *   Returns a server-side generated airavata compute resource globally unique identifier.
   *
  */
  string registerComputeResource(1: required security_model.AuthzToken authzToken,
                                 2: required compute_resource_model.ComputeResourceDescription computeResourceDescription)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch the given Compute Resource.
   *
   * @param computeResourceId
   *   The identifier for the requested compute resource
   *
   * @return computeResourceDescription
   *    Compute Resource Object created from the datamodel..
   *
  */
  compute_resource_model.ComputeResourceDescription getComputeResource(1: required security_model.AuthzToken authzToken,
                2: required string computeResourceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch all registered Compute Resources.
   *
   * @return A map of registered compute resource id's and thier corresponding hostnames.
   *    Compute Resource Object created from the datamodel..
   *
  */
  map<string, string> getAllComputeResourceNames(1: required security_model.AuthzToken authzToken)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Compute Resource.
   *
   * @param computeResourceId
   *   The identifier for the requested compute resource to be updated.
   *
   * @param computeResourceDescription
   *    Compute Resource Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateComputeResource(1: required security_model.AuthzToken authzToken,
            2: required string computeResourceId,
            3: required compute_resource_model.ComputeResourceDescription computeResourceDescription)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a Compute Resource.
   *
   * @param computeResourceId
   *   The identifier for the requested compute resource to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteComputeResource(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   * Add a Local Job Submission details to a compute resource
   *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param localSubmission
   *   The LOCALSubmission object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */
  string addLocalSubmissionDetails(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.LOCALSubmission localSubmission)

  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Update the given Local Job Submission details
   *
   * @param jobSubmissionInterfaceId
   *   The identifier of the JobSubmission Interface to be updated.
   *
   * @param localSubmission
   *   The LOCALSubmission object to be updated.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool updateLocalSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string jobSubmissionInterfaceId,
            3: required compute_resource_model.LOCALSubmission localSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
  * This method returns localJobSubmission object
  * @param jobSubmissionInterfaceId
  *   The identifier of the JobSubmission Interface to be retrieved.
  *  @return LOCALSubmission instance
  **/
  compute_resource_model.LOCALSubmission getLocalJobSubmission(1: required security_model.AuthzToken authzToken,
                    2: required string jobSubmissionId)
            throws (1: airavata_errors.InvalidRequestException ire,
                    2: airavata_errors.AiravataClientException ace,
                    3: airavata_errors.AiravataSystemException ase,
                    4: airavata_errors.AuthorizationException ae)



  /**
   * Add a SSH Job Submission details to a compute resource
   *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param sshJobSubmission
   *   The SSHJobSubmission object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */


  string addSSHJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.SSHJobSubmission sshJobSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Add a SSH_FORK Job Submission details to a compute resource
   *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param sshJobSubmission
   *   The SSHJobSubmission object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */


  string addSSHForkJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.SSHJobSubmission sshJobSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

    /**
    * This method returns SSHJobSubmission object
    * @param jobSubmissionInterfaceId
    *   The identifier of the JobSubmission Interface to be retrieved.
    *  @return SSHJobSubmission instance
    **/
    compute_resource_model.SSHJobSubmission getSSHJobSubmission(1: required security_model.AuthzToken authzToken,
                      2: required string jobSubmissionId)
              throws (1: airavata_errors.InvalidRequestException ire,
                      2: airavata_errors.AiravataClientException ace,
                      3: airavata_errors.AiravataSystemException ase,
                      4: airavata_errors.AuthorizationException ae)



  /**
   * Add a UNICORE Job Submission details to a compute resource
   *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param unicoreJobSubmission
   *   The UnicoreJobSubmission object to be added to the resource.
   *
   * @return status
   *  Returns the unique job submission id.
   *
  */
  string addUNICOREJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.UnicoreJobSubmission unicoreJobSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)


  /**
    * This method returns UnicoreJobSubmission object
    * @param jobSubmissionInterfaceId
    *   The identifier of the JobSubmission Interface to be retrieved.
    *  @return UnicoreJobSubmission instance
  **/
  compute_resource_model.UnicoreJobSubmission getUnicoreJobSubmission(1: required security_model.AuthzToken authzToken,
                        2: required string jobSubmissionId)
                throws (1: airavata_errors.InvalidRequestException ire,
                        2: airavata_errors.AiravataClientException ace,
                        3: airavata_errors.AiravataSystemException ase,
                        4: airavata_errors.AuthorizationException ae)


 /**
   * Add a Cloud Job Submission details to a compute resource
   *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param sshJobSubmission
   *   The SSHJobSubmission object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
**/
 string addCloudJobSubmissionDetails(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.CloudJobSubmission cloudSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

 /**
    * This method returns cloudJobSubmission object
    * @param jobSubmissionInterfaceI
        *   The identifier of the JobSubmission Interface to be retrieved.
    *  @return CloudJobSubmission instance
 **/
 compute_resource_model.CloudJobSubmission getCloudJobSubmission(1: required security_model.AuthzToken authzToken, 2: required string jobSubmissionId)
                  throws (1: airavata_errors.InvalidRequestException ire,
                          2: airavata_errors.AiravataClientException ace,
                          3: airavata_errors.AiravataSystemException ase,
                          4: airavata_errors.AuthorizationException ae)

  /**
   * Update the given SSH Job Submission details
   *
   * @param jobSubmissionInterfaceId
   *   The identifier of the JobSubmission Interface to be updated.
   *
   * @param sshJobSubmission
   *   The SSHJobSubmission object to be updated.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool updateSSHJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string jobSubmissionInterfaceId,
            3: required compute_resource_model.SSHJobSubmission sshJobSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

/**
   * Update the cloud Job Submission details
   *
   * @param jobSubmissionInterfaceId
   *   The identifier of the JobSubmission Interface to be updated.
   *
   * @param cloudJobSubmission
   *   The CloudJobSubmission object to be updated.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool updateCloudJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
            2: required string jobSubmissionInterfaceId,
            3: required compute_resource_model.CloudJobSubmission sshJobSubmission)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  bool updateUnicoreJobSubmissionDetails(1: required security_model.AuthzToken authzToken,
              2: required string jobSubmissionInterfaceId,
              3: required compute_resource_model.UnicoreJobSubmission unicoreJobSubmission)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)
  /**
   * Add a Local data movement details to a compute resource
   *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param localDataMovement
   *   The LOCALDataMovement object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */
  string addLocalDataMovementDetails(1: required security_model.AuthzToken authzToken,
            2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.LOCALDataMovement localDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Update the given Local data movement details
   *
   * @param dataMovementInterfaceId
   *   The identifier of the data movement Interface to be updated.
   *
   * @param localDataMovement
   *   The LOCALDataMovement object to be updated.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateLocalDataMovementDetails(1: required security_model.AuthzToken authzToken,
            2: required string dataMovementInterfaceId,
            3: required compute_resource_model.LOCALDataMovement localDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
          * This method returns local datamovement object
          * @param dataMovementId
          *   The identifier of the datamovement Interface to be retrieved.
          *  @return LOCALDataMovement instance
  **/
  compute_resource_model.LOCALDataMovement getLocalDataMovement(1: required security_model.AuthzToken authzToken,
                    2: required string dataMovementId)
                    throws (1: airavata_errors.InvalidRequestException ire,
                            2: airavata_errors.AiravataClientException ace,
                            3: airavata_errors.AiravataSystemException ase,
                            4: airavata_errors.AuthorizationException ae)


  /**
   * Add a SCP data movement details to a compute resource
   *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param scpDataMovement
   *   The SCPDataMovement object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */
  string addSCPDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.SCPDataMovement scpDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Update the given scp data movement details
   *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
   *
   * @param dataMovementInterfaceId
   *   The identifier of the data movement Interface to be updated.
   *
   * @param scpDataMovement
   *   The SCPDataMovement object to be updated.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateSCPDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string dataMovementInterfaceId,
            3: required compute_resource_model.SCPDataMovement scpDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

 /**
    * This method returns SCP datamovement object
    * @param dataMovementId
       *   The identifier of the datamovement Interface to be retrieved.
       *  @return SCPDataMovement instance
  **/
  compute_resource_model.SCPDataMovement getSCPDataMovement(1: required security_model.AuthzToken authzToken, 2: required string dataMovementId)
                    throws (1: airavata_errors.InvalidRequestException ire,
                            2: airavata_errors.AiravataClientException ace,
                            3: airavata_errors.AiravataSystemException ase,
                            4: airavata_errors.AuthorizationException ae)


 string addUnicoreDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
              3: required i32 priorityOrder,
              4: required compute_resource_model.UnicoreDataMovement unicoreDataMovement)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

 bool updateUnicoreDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string dataMovementInterfaceId,
             3: required compute_resource_model.UnicoreDataMovement unicoreDataMovement)
   	throws (1: airavata_errors.InvalidRequestException ire,
             2: airavata_errors.AiravataClientException ace,
             3: airavata_errors.AiravataSystemException ase,
             4: airavata_errors.AuthorizationException ae)

 compute_resource_model.UnicoreDataMovement getUnicoreDataMovement(1: required security_model.AuthzToken authzToken,
                     2: required string dataMovementId)
                     throws (1: airavata_errors.InvalidRequestException ire,
                             2: airavata_errors.AiravataClientException ace,
                             3: airavata_errors.AiravataSystemException ase,
                             4: airavata_errors.AuthorizationException ae)

  /**
   * Add a GridFTP data movement details to a compute resource
   *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
   *
   * @param computeResourceId
   *   The identifier of the compute resource to which JobSubmission protocol to be added
   *
   * @param priorityOrder
   *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
   *
   * @param gridFTPDataMovement
   *   The GridFTPDataMovement object to be added to the resource.
   *
   * @return status
   *   Returns the unique job submission id.
   *
  */
  string addGridFTPDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required i32 priorityOrder,
            4: required compute_resource_model.GridFTPDataMovement gridFTPDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Update the given GridFTP data movement details to a compute resource
   *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
   *
   * @param dataMovementInterfaceId
   *   The identifier of the data movement Interface to be updated.
   *
   * @param gridFTPDataMovement
   *   The GridFTPDataMovement object to be updated.
   *
   * @return status
   *   Returns a success/failure of the updation.
   *
  */
  bool updateGridFTPDataMovementDetails(1: required security_model.AuthzToken authzToken, 2: required string dataMovementInterfaceId,
            3: required compute_resource_model.GridFTPDataMovement gridFTPDataMovement)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

 /**
    * This method returns GridFTP datamovement object
    * @param dataMovementId
       *   The identifier of the datamovement Interface to be retrieved.
    *  @return GridFTPDataMovement instance
  **/
  compute_resource_model.GridFTPDataMovement getGridFTPDataMovement(1: required security_model.AuthzToken authzToken, 2: required string dataMovementId)
                    throws (1: airavata_errors.InvalidRequestException ire,
                            2: airavata_errors.AiravataClientException ace,
                            3: airavata_errors.AiravataSystemException ase,
                            4: airavata_errors.AuthorizationException ae)


  /**
   * Change the priority of a given job submisison interface
   *
   * @param jobSubmissionInterfaceId
   *   The identifier of the JobSubmission Interface to be changed
   *
   * @param priorityOrder
   *   The new priority of the job manager interface.
   *
   * @return status
   *   Returns a success/failure of the change.
   *
  */
  bool changeJobSubmissionPriority(1: required security_model.AuthzToken authzToken, 2: required string jobSubmissionInterfaceId,
            3: required i32 newPriorityOrder)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Change the priority of a given data movement interface
   *
   * @param dataMovementInterfaceId
   *   The identifier of the DataMovement Interface to be changed
   *
   * @param priorityOrder
   *   The new priority of the data movement interface.
   *
   * @return status
   *   Returns a success/failure of the change.
   *
  */
  bool changeDataMovementPriority(1: required security_model.AuthzToken authzToken, 2: required string dataMovementInterfaceId,
            3: required i32 newPriorityOrder)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Change the priorities of a given set of job submission interfaces
   *
   * @param jobSubmissionPriorityMap
   *   A Map of identifiers of the JobSubmission Interfaces and thier associated priorities to be set.
   *
   * @return status
   *   Returns a success/failure of the changes.
   *
  */
  bool changeJobSubmissionPriorities(1: required security_model.AuthzToken authzToken, 2: required map<string, i32> jobSubmissionPriorityMap)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Change the priorities of a given set of data movement interfaces
   *
   * @param dataMovementPriorityMap
   *   A Map of identifiers of the DataMovement Interfaces and thier associated priorities to be set.
   *
   * @return status
   *   Returns a success/failure of the changes.
   *
  */
  bool changeDataMovementPriorities(1: required security_model.AuthzToken authzToken, 2: required map<string, i32> dataMovementPriorityMap)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a given job submisison interface
   *
   * @param jobSubmissionInterfaceId
   *   The identifier of the JobSubmission Interface to be changed
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteJobSubmissionInterface(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required string jobSubmissionInterfaceId)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  /**
   * Delete a given data movement interface
   *
   * @param dataMovementInterfaceId
   *   The identifier of the DataMovement Interface to be changed
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteDataMovementInterface(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId,
            3: required string dataMovementInterfaceId)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

 string registerResourceJobManager(1: required security_model.AuthzToken authzToken, 2: required compute_resource_model.ResourceJobManager resourceJobManager)
    throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

 bool updateResourceJobManager(1: required security_model.AuthzToken authzToken, 2: required string resourceJobManagerId,
            3: required compute_resource_model.ResourceJobManager updatedResourceJobManager)
     throws (1: airavata_errors.InvalidRequestException ire,
             2: airavata_errors.AiravataClientException ace,
             3: airavata_errors.AiravataSystemException ase,
             4: airavata_errors.AuthorizationException ae)

 compute_resource_model.ResourceJobManager getResourceJobManager(1: required security_model.AuthzToken authzToken, 2: required string resourceJobManagerId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

 bool deleteResourceJobManager(1: required security_model.AuthzToken authzToken, 2: required string resourceJobManagerId)
       throws (1: airavata_errors.InvalidRequestException ire,
               2: airavata_errors.AiravataClientException ace,
               3: airavata_errors.AiravataSystemException ase,
               4: airavata_errors.AuthorizationException ae)

  bool deleteBatchQueue(1: required security_model.AuthzToken authzToken, 2: required string computeResourceId, 3: required string queueName)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)
/*
 * Gateway Resource Profile
 *
*/

  /**
   * Register a Gateway Resource Profile.
   *
   * @param gatewayResourceProfile
   *    Gateway Resource Profile Object.
   *    The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
   *      resource profile.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  string registerGatewayResourceProfile(1: required security_model.AuthzToken authzToken,
                    2: required gateway_resource_profile_model.GatewayResourceProfile gatewayResourceProfile)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch the given Gateway Resource Profile.
   *
   * @param gatewayID
   *   The identifier for the requested gateway resource
   *
   * @return gatewayResourceProfile
   *    Gateway Resource Profile Object.
   *
  */
  gateway_resource_profile_model.GatewayResourceProfile getGatewayResourceProfile(1: required security_model.AuthzToken authzToken,
                2: required string gatewayID)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Gateway Resource Profile.
   *
   * @param gatewayID
   *   The identifier for the requested gateway resource to be updated.
   *
   * @param gatewayResourceProfile
   *    Gateway Resource Profile Object.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateGatewayResourceProfile(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
            3: required gateway_resource_profile_model.GatewayResourceProfile gatewayResourceProfile)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Delete the given Gateway Resource Profile.
   *
   * @param gatewayID
   *   The identifier for the requested gateway resource to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteGatewayResourceProfile(1: required security_model.AuthzToken authzToken, 2: required string gatewayID)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   * Add a Compute Resource Preference to a registered gateway profile.
   *
   * @param gatewayID
   *   The identifier for the gateway profile to be added.
   *
   * @param computeResourceId
   *   Preferences related to a particular compute resource
   *
   * @param computeResourcePreference
   *   The ComputeResourcePreference object to be added to the resource profile.
   *
   * @return status
   *   Returns a success/failure of the addition. If a profile already exists, this operation will fail.
   *    Instead an update should be used.
   *
  */
  bool addGatewayComputeResourcePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
            3: required string computeResourceId,
            4: required gateway_resource_profile_model.ComputeResourcePreference computeResourcePreference)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  bool addGatewayDataStoragePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
              3: required string dataMoveId,
              4: required gateway_resource_profile_model.DataStoragePreference dataStoragePreference)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)
  /**
   * Fetch a Compute Resource Preference of a registered gateway profile.
   *
   * @param gatewayID
   *   The identifier for the gateway profile to be requested
   *
   * @param computeResourceId
   *   Preferences related to a particular compute resource
   *
   * @return computeResourcePreference
   *   Returns the ComputeResourcePreference object.
   *
  */
  gateway_resource_profile_model.ComputeResourcePreference getGatewayComputeResourcePreference(1: required security_model.AuthzToken authzToken,
            2: required string gatewayID,
            3: required string computeResourceId)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  gateway_resource_profile_model.DataStoragePreference getGatewayDataStoragePreference(1: required security_model.AuthzToken authzToken,
              2: required string gatewayID,
              3: required string dataMoveId)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch all Compute Resource Preferences of a registered gateway profile.
   *
   * @param gatewayID
   *   The identifier for the gateway profile to be requested
   *
   * @return computeResourcePreference
   *   Returns the ComputeResourcePreference object.
   *
  */
  list<gateway_resource_profile_model.ComputeResourcePreference>
            getAllGatewayComputeResourcePreferences(1: required security_model.AuthzToken authzToken, 2: required string gatewayID)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)


  list<gateway_resource_profile_model.DataStoragePreference>
              getAllGatewayDataStoragePreferences(1: required security_model.AuthzToken authzToken, 2: required string gatewayID)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
  * Fetch all gateway profiles registered
  **/
  list<gateway_resource_profile_model.GatewayResourceProfile>
              getAllGatewayResourceProfiles(1: required security_model.AuthzToken authzToken)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Update a Compute Resource Preference to a registered gateway profile.
   *
   * @param gatewayID
   *   The identifier for the gateway profile to be updated.
   *
   * @param computeResourceId
   *   Preferences related to a particular compute resource
   *
   * @param computeResourcePreference
   *   The ComputeResourcePreference object to be updated to the resource profile.
   *
   * @return status
   *   Returns a success/failure of the updation.
   *
  */
  bool updateGatewayComputeResourcePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
            3: required string computeResourceId,
            4: required gateway_resource_profile_model.ComputeResourcePreference computeResourcePreference)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  bool updateGatewayDataStoragePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
              3: required string dataMoveId,
              4: required gateway_resource_profile_model.DataStoragePreference dataStoragePreference)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   * Delete the Compute Resource Preference of a registered gateway profile.
   *
   * @param gatewayID
   *   The identifier for the gateway profile to be deleted.
   *
   * @param computeResourceId
   *   Preferences related to a particular compute resource
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteGatewayComputeResourcePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
            3: required string computeResourceId)
  	throws (1: airavata_errors.InvalidRequestException ire,
            2: airavata_errors.AiravataClientException ace,
            3: airavata_errors.AiravataSystemException ase,
            4: airavata_errors.AuthorizationException ae)

  bool deleteGatewayDataStoragePreference(1: required security_model.AuthzToken authzToken, 2: required string gatewayID,
              3: required string dataMoveId)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  list<string> getAllWorkflows(1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  workflow_data_model.Workflow getWorkflow (1: required security_model.AuthzToken authzToken, 2: required string workflowTemplateId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  void deleteWorkflow (1: required security_model.AuthzToken authzToken, 2: required string workflowTemplateId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  string registerWorkflow(1: required security_model.AuthzToken authzToken, 2: required string gatewayId,
                          3: required workflow_data_model.Workflow workflow)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  void updateWorkflow (1: required security_model.AuthzToken authzToken, 2: required string workflowTemplateId,
                       3: required workflow_data_model.Workflow workflow)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  string getWorkflowTemplateId (1: required security_model.AuthzToken authzToken, 2: required string workflowName)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  bool isWorkflowExistWithName(1: required security_model.AuthzToken authzToken, 2: required string workflowName)
          throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

 //End of API
 }

