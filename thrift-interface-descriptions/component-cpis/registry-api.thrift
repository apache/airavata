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
 * Component Programming Interface definition for Apache Airavata Registry Service.
 *
*/

include "../data-models/airavata_data_models.thrift"
include "../data-models/user-tenant-group-models/user_profile_model.thrift"
include "../data-models/experiment-catalog-models/status_models.thrift"
include "../data-models/experiment-catalog-models/job_model.thrift"
include "../data-models/experiment-catalog-models/process_model.thrift"
include "../data-models/experiment-catalog-models/task_model.thrift"
include "../data-models/experiment-catalog-models/experiment_model.thrift"
include "../data-models/experiment-catalog-models/workspace_model.thrift"
include "../data-models/experiment-catalog-models/scheduling_model.thrift"
include "../data-models/app-catalog-models/application_io_models.thrift"
include "../data-models/app-catalog-models/application_deployment_model.thrift"
include "../data-models/app-catalog-models/application_interface_model.thrift"
include "../data-models/app-catalog-models/parser_model.thrift"
include "../data-models/resource-catalog-models/compute_resource_model.thrift"
include "../data-models/resource-catalog-models/storage_resource_model.thrift"
include "../data-models/resource-catalog-models/group_resource_profile_model.thrift"
include "../data-models/resource-catalog-models/gateway_resource_profile_model.thrift"
include "../data-models/resource-catalog-models/gateway_groups_model.thrift"
include "../data-models/resource-catalog-models/user_resource_profile_model.thrift"
include "../data-models/resource-catalog-models/data_movement_models.thrift"
include "../data-models/replica-catalog-models/replica_catalog_models.thrift"
include "../airavata-apis/airavata_errors.thrift"
include "../airavata-apis/airavata_commons.thrift"
include "../base-api/base_api.thrift"

include "registry_api_errors.thrift"

namespace java org.apache.airavata.registry.api

const string REGISTRY_API_VERSION = "0.18.0"

service RegistryService extends base_api.BaseAPI {

     /**
     * Verify if User Exists within Airavata.
     *
     * @param gatewayId
     *
     *  @param userName
     *
     * @return true/false
     *
     **/
      bool isUserExists (1: required string gatewayId,
                         2: required string userName)
              throws (1: registry_api_errors.RegistryServiceException rse)

       /**
       * Register a Gateway with Airavata.
       *
       * @param gateway
       *    The gateway data model.
       *
       * @return gatewayId
       *   Th unique identifier of the  newly registered gateway.
       *
       **/
      string addGateway(1: required workspace_model.Gateway gateway)
             throws (1: registry_api_errors.RegistryServiceException rse, 2: airavata_errors.DuplicateEntryException dee)


      /**
         * Get all users in the gateway
         *
         * @param gatewayId
         *    The gateway data model.
         *
         * @return users
         *   list of usernames of the users in the gateway
         *
         **/
      list<string> getAllUsersInGateway(1: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse)

       /**
       * Update previously registered Gateway metadata.
       *
       * @param gatewayId
       *    The gateway Id of the Gateway which require an update.
       *
       * @return gateway
       *    Modified gateway obejct.
       *
       * @exception AiravataClientException
       *
       **/

      bool updateGateway(1: required string gatewayId, 2: required workspace_model.Gateway updatedGateway)
             throws (1: registry_api_errors.RegistryServiceException rse)

        /**
        * Get Gateway details by providing gatewayId
        *
        * @param gatewayId
        *    The gateway Id of the Gateway.
        *
        * @return gateway
        *    Gateway obejct.
        *
        **/

      workspace_model.Gateway getGateway(1: required string gatewayId)
               throws (1: registry_api_errors.RegistryServiceException rse)

        /**
        * Delete a Gateway
        *
        * @param gatewayId
        *    The gateway Id of the Gateway to be deleted.
        *
        * @return boolean
        *    Boolean identifier for the success or failure of the deletion operation.
        *
        **/

      bool deleteGateway(1: required string gatewayId)
                 throws (1: registry_api_errors.RegistryServiceException rse)

        /**
        * Get All the Gateways Connected to Airavata.
        **/

      list<workspace_model.Gateway> getAllGateways()
                 throws (1: registry_api_errors.RegistryServiceException rse)

        /**
        * Check for the Existance of a Gateway within Airavata
        *
        * @param gatewayId
        *   Provide the gatewayId of the gateway you want to check the existancy
        *
        * @return boolean
        *   Boolean idetifier for the existance or non-existane of the gatewayId
        *
        * @return gatewayId
        *   return the gatewayId of the existing gateway.
        *
        **/
      bool isGatewayExist(1: required string gatewayId)
               throws (1: registry_api_errors.RegistryServiceException rse)

      /**
        * API methods to retrieve notifications
      **/
       string createNotification(1: required workspace_model.Notification notification)
            throws (1: registry_api_errors.RegistryServiceException rse)

       bool updateNotification(1: required workspace_model.Notification notification)
               throws (1: registry_api_errors.RegistryServiceException rse)


      bool deleteNotification(1: required string gatewayId, 2: required string notificationId)
                 throws (1: registry_api_errors.RegistryServiceException rse)

      workspace_model.Notification getNotification(1: required string gatewayId, 2: required string notificationId)
                 throws (1: registry_api_errors.RegistryServiceException rse)

      list<workspace_model.Notification> getAllNotifications(1: required string gatewayId)
                 throws (1: registry_api_errors.RegistryServiceException rse)


      /**
         *
         * Creates a Project with basic metadata.
         *    A Project is a container of experiments.
         *
         * @param gatewayId
         *    The identifier for the requested gateway.
         *
         * @param Project
         *    The Project Object described in the workspace_model.
         *
         **/
        string createProject (1: required string gatewayId,
                              2: required workspace_model.Project project)
                throws (1: registry_api_errors.RegistryServiceException rse)

         /**
         *
         * Update an Existing Project
         *
         * @param projectId
         *    The projectId of the project needed an update.
         *
         * @return void
         *    Currently this does not return any value.
         *
         **/
        void updateProject (1: required string projectId,
                            2: required workspace_model.Project updatedProject)
            throws (1: registry_api_errors.RegistryServiceException rse,
                    2: airavata_errors.ProjectNotFoundException pnfe)

         /**
         *
         * Get a Project by ID
         *    This method is to obtain a project by providing a projectId.
         *
         * @param projectId
         *    projectId of the project you require.
         *
         * @return project
         *    project data model will be returned.
         *
         **/
        workspace_model.Project getProject (1: required string projectId)
              throws (1: registry_api_errors.RegistryServiceException rse,
                      2: airavata_errors.ProjectNotFoundException pnfe)

         /**
         *
         * Delete a Project
         *    This method is used to delete an existing Project.
         *
         * @param projectId
         *    projectId of the project you want to delete.
         *
         * @return boolean
         *    Boolean identifier for the success or failure of the deletion operation.
         *
         *    NOTE: This method is not used within gateways connected with Airavata.
         *
         **/
        bool deleteProject (1: required string projectId)
                throws (1: registry_api_errors.RegistryServiceException rse,
                        2: airavata_errors.ProjectNotFoundException pnfe)

         /**
         *
         * Get All User Projects
         * Get all Project for the user with pagination. Results will be ordered based on creation time DESC.
         *
         * @param gatewayId
         *    The identifier for the requested gateway.
         *
         * @param userName
         *    The identifier of the user.
         *
         * @param limit
         *    The amount results to be fetched.
         *
         * @param offset
         *    The starting point of the results to be fetched.
         *
         **/
        list<workspace_model.Project> getUserProjects(1: required string gatewayId,
                                                         2: required string userName,
                                                         3: required i32 limit,
                                                         4: required i32 offset)
              throws (1: registry_api_errors.RegistryServiceException rse)


          /**
          *
          * Search User Projects
          * Search and get all Projects for user by project description or/and project name  with pagination.
          * Results will be ordered based on creation time DESC.
          *
          * @param gatewayId
          *    The unique identifier of the gateway making the request.
          *
          * @param userName
          *    The identifier of the user.
          *
          * @param filters
          *    Map of multiple filter criteria. Currenlt search filters includes Project Name and Project Description
          *
          * @param limit
          *    The amount results to be fetched.
          *
          * @param offset
          *    The starting point of the results to be fetched.
          *
          **/
        list<workspace_model.Project> searchProjects(
                                    1: required string gatewayId,
                                    2: required string userName,
                                    3: required list<string> accessibleProjIds,
                                    4: map<experiment_model.ProjectSearchFields, string> filters,
                                    5: required i32 limit,
                                    6: required i32 offset)
                        throws (1: registry_api_errors.RegistryServiceException rse)



        /**
           * Search Experiments.
           * Search Experiments by using multiple filter criteria with pagination. Results will be sorted based on creation time DESC.
           *
           * @param gatewayId
           *       Identifier of the requested gateway.
           *
           * @param userName
           *       Username of the user requesting the search function.
           *
           * @param filters
           *       Map of multiple filter criteria. Currenlt search filters includes Experiment Name, Description, Application, etc....
           *
           * @param limit
           *       Amount of results to be fetched.
           *
           * @param offset
           *       The starting point of the results to be fetched.
           *
           * @return ExperimentSummaryModel
           *    List of experiments for the given search filter. Here only the Experiment summary will be returned.
           *
           **/

            list<experiment_model.ExperimentSummaryModel> searchExperiments(1: required string gatewayId,
                                    2: required string userName,
                                    3: required list<string> accessibleExpIds,
                                    4: map<experiment_model.ExperimentSearchFields, string> filters,
                                    5: required i32 limit,
                                    6: required i32 offset)
                        throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Get Experiment Statistics
             * Get Experiment Statisitics for a given gateway for a specific time period. This feature is available only for admins of a particular gateway. Gateway admin access is managed by the user roles.
             *
             * @param gatewayId
             *       Unique identifier of the gateway making the request to fetch statistics.
             *
             * @param fromTime
             *       Starting date time.
             *
             * @param toTime
             *       Ending data time.
             *
             * @param userName
             *       Gateway username substring with which to further filter statistics.
             *
             * @param applicationName
             *       Application id substring with which to further filter statistics.
             *
             * @param resourceHostName
             *       Hostname id substring with which to further filter statistics.
             *
             * @param accessibleExpIds
             *    Experiment IDs which are accessible to the current user.
             *
             * @param limit
             *       Amount of results to be fetched.
             *
             * @param offset
             *       The starting point of the results to be fetched.
             *
             **/
            experiment_model.ExperimentStatistics getExperimentStatistics(1: required string gatewayId,
                                    2: required i64 fromTime,
                                    3: required i64 toTime,
                                    4: string userName,
                                    5: string applicationName,
                                    6: string resourceHostName,
                                    7: list<string> accessibleExpIds,
                                    8: i32 limit = 50,
                                    9: i32 offset = 0)
                        throws (1: registry_api_errors.RegistryServiceException rse)


          /**
           *
           * Get All Experiments of the Project
           * Get Experiments within project with pagination. Results will be sorted based on creation time DESC.
           *
           * @param projectId
           *       Uniqie identifier of the project.
           *
           * @param limit
           *       Amount of results to be fetched.
           *
           * @param offset
           *       The starting point of the results to be fetched.
           *
           **/
          list<experiment_model.ExperimentModel> getExperimentsInProject(1: required string gatewayId,
                          2: required string projectId,
                          3: required i32 limit,
                          4: required i32 offset)
                  throws (1: registry_api_errors.RegistryServiceException rse,
                          2: airavata_errors.ProjectNotFoundException pnfe)

           /**
           *
           * Get All Experiments of the User
           * Get experiments by user with pagination. Results will be sorted based on creation time DESC.
           *
           * @param gatewayId
           *       Identifier of the requesting gateway.
           *
           * @param userName
           *       Username of the requested end user.
           *
           * @param limit
           *       Amount of results to be fetched.
           *
           * @param offset
           *       The starting point of the results to be fetched.
           *
           **/
          list<experiment_model.ExperimentModel> getUserExperiments(1: required string gatewayId,
                                2: required string userName,
                                3: required i32 limit,
                                4: required i32 offset)
                    throws (1: registry_api_errors.RegistryServiceException rse)

           /**
             *
             * Create New Experiment
             * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
             *   but inferred from the sshKeyAuthentication header. This experiment is just a persistent place holder. The client
             *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
             *   registering the experiment in a persistent store.
             *
             * @param gatewayId
             *    The unique ID of the gateway where the experiment is been created.
             *
             * @param ExperimentModel
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
           **/
          string createExperiment(1: required string gatewayId,
                                  2: required experiment_model.ExperimentModel experiment)
            throws (1: registry_api_errors.RegistryServiceException rse)

          /**
          *
          * Delete an Experiment
          * If the experiment is not already launched experiment can be deleted.
          *
          * @param authzToken
          *
          * @param experiementId
          *     Experiment ID of the experimnet you want to delete.
          *
          * @return boolean
          *     Identifier for the success or failure of the deletion operation.
          *
          **/
          bool deleteExperiment(1: required string experimentId)
            throws (1: registry_api_errors.RegistryServiceException rse)


          /**
           *
           * Get Experiment
           * Fetch previously created experiment metadata.
           *
           * @param airavataExperimentId
           *    The unique identifier of the requested experiment. This ID is returned during the create experiment step.
           *
           * @return ExperimentModel
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
         **/
          experiment_model.ExperimentModel getExperiment(1: required string airavataExperimentId)
            throws (1: registry_api_errors.RegistryServiceException rse,
                    2: airavata_errors.ExperimentNotFoundException enf)


          /**
           *
           * Get Complete Experiment Details
           * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
           * tasks -> jobs information.
           *
           * @param airavataExperimentId
           *    The identifier for the requested experiment. This is returned during the create experiment step.
           *
           * @return ExperimentModel
           *   This method will return the previously stored experiment metadata including application input parameters, computational resource scheduling
           *   information, special input output handling and additional quality of service parameters.
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
          experiment_model.ExperimentModel getDetailedExperimentTree(1: required string airavataExperimentId)
              throws (1: registry_api_errors.RegistryServiceException rse,
                      2: airavata_errors.ExperimentNotFoundException enf)


          /**
           *
           * Update a Previously Created Experiment
           * Configure the CREATED experiment with required inputs, scheduling and other quality of service parameters. This method only updates the experiment object within the registry.
           * The experiment has to be launched to make it actionable by the server.
           *
           * @param airavataExperimentId
           *    The identifier for the requested experiment. This is returned during the create experiment step.
           *
           * @param ExperimentModel
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
          void updateExperiment(1: required string airavataExperimentId,
                                2: required experiment_model.ExperimentModel experiment)
            throws (1: registry_api_errors.RegistryServiceException rse,
                    2: airavata_errors.ExperimentNotFoundException enf)

          void updateExperimentConfiguration(1: required string airavataExperimentId,
                                             2: required experiment_model.UserConfigurationDataModel userConfiguration)
            throws (1: registry_api_errors.RegistryServiceException rse)

          void updateResourceScheduleing(1: required string airavataExperimentId,
                                         2: required scheduling_model.ComputationalResourceSchedulingModel resourceScheduling)
            throws (1: registry_api_errors.RegistryServiceException rse)


         /**
           *
           * Get Experiment Status
           *
           * Obtain the status of an experiment by providing the Experiment Id
           *
           * @param authzToken
           *
           * @param airavataExperimentId
           *     Experiment ID of the experimnet you require the status.
           *
           * @return ExperimentStatus
           *     ExperimentStatus model with the current status will be returned.
           *
           **/
            status_models.ExperimentStatus getExperimentStatus(1: required string airavataExperimentId)
               throws (1: registry_api_errors.RegistryServiceException rse,
                       2: airavata_errors.ExperimentNotFoundException enf)

           /**
           *
           * Get Experiment Outputs
           * This method to be used when need to obtain final outputs of a certain Experiment
           *
           * @param authzToken
           *
           * @param airavataExperimentId
           *     Experiment ID of the experimnet you need the outputs.
           *
           * @return list
           *     List of experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
           *
           **/
           list<application_io_models.OutputDataObjectType> getExperimentOutputs (1: required string airavataExperimentId)
               throws (1: registry_api_errors.RegistryServiceException rse,
                       2: airavata_errors.ExperimentNotFoundException enf)

           /**
           *
           * Get Intermediate Experiment Outputs
           * This method to be used when need to obtain intermediate outputs of a certain Experiment
           *
           * @param authzToken
           *
           * @param airavataExperimentId
           *     Experiment ID of the experimnet you need intermediate outputs.
           *
           * @return list
           *     List of intermediate experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
           *
           **/
            list<application_io_models.OutputDataObjectType> getIntermediateOutputs (1: required string airavataExperimentId)
                 throws (1: registry_api_errors.RegistryServiceException rse,
                         2: airavata_errors.ExperimentNotFoundException enf)

           /**
           *
           * Get Job Statuses for an Experiment
           * This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup
           *
           * @param authzToken
           *
           * @param experiementId
           *     Experiment ID of the experimnet you need the job statuses.
           *
           * @return JobStatus
           *     Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map
           *
           **/
           map<string, status_models.JobStatus> getJobStatuses(1: required string airavataExperimentId)
                                  throws (1: registry_api_errors.RegistryServiceException rse,
                                          2: airavata_errors.ExperimentNotFoundException enf)

           /**
           *
           * Add Experiment/Process outputs
           *
           **/
           void addExperimentProcessOutputs (1: required string outputType, 2: required list<application_io_models.OutputDataObjectType> outputs, 3: required string id)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           /**
           *
           * Add Errors
           * EXPERIMENT_ERROR, PROCESS_ERROR, TASK_ERROR
           *
           **/
           void addErrors (1: required string errorType, 2: required airavata_commons.ErrorModel errorModel, 3: string id)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void addTaskStatus (1: required status_models.TaskStatus taskStatus, 2: required string taskId )
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void addProcessStatus (1: required status_models.ProcessStatus processStatus, 2: required string processId )
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void updateProcessStatus (1: required status_models.ProcessStatus processStatus, 2: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void updateExperimentStatus (1: required status_models.ExperimentStatus experimentStatus, 2: required string experimentId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void addJobStatus (1: required status_models.JobStatus jobStatus, 2: required string taskId, 3: required string jobId )
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void addJob (1: required job_model.JobModel jobModel, 2: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           string addProcess (1: required process_model.ProcessModel processModel, 2: required string experimentId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           void updateProcess (1: required process_model.ProcessModel processModel, 2: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           string addTask (1: required task_model.TaskModel taskModel, 2: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           experiment_model.UserConfigurationDataModel getUserConfigurationData(1: required string experimentId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           process_model.ProcessModel getProcess(1: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           list<process_model.ProcessModel> getProcessList(1: required string experimentId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           status_models.ProcessStatus getProcessStatus(1: required string processId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           /*
           * queryType can be TASK_ID OR PROCESS_ID
           *
           */
           bool isJobExist(1: required string queryType, 2: required string id)
                       throws (1: registry_api_errors.RegistryServiceException rse)

           /*
           * queryType can be TASK_ID OR PROCESS_ID
           *
           */
           job_model.JobModel getJob(1: required string queryType, 2: required string id)
                       throws (1: registry_api_errors.RegistryServiceException rse)

           /*
           * queryType can be TASK_ID, PROCESS_ID or JOB_ID
           *
           */
           list<job_model.JobModel> getJobs(1: required string queryType, 2: required string id)
                                  throws (1: registry_api_errors.RegistryServiceException rse)


           list<application_io_models.OutputDataObjectType> getProcessOutputs (1: required string processId)
                       throws (1: registry_api_errors.RegistryServiceException rse)

           /*
           * Returns number of Helix workflows that are currently registered for the process id
           */
           list<process_model.ProcessWorkflow> getProcessWorkflows(1: required string processId)
                       throws (1: registry_api_errors.RegistryServiceException rse)

           /*
           * Register a new Helix workflow to process
           */
           void addProcessWorkflow(1: required process_model.ProcessWorkflow processWorkflow)
                        throws (1: registry_api_errors.RegistryServiceException rse)

           list<string> getProcessIds(1: required string experimentId)
                        throws (1: registry_api_errors.RegistryServiceException rse)
           /**
           *
           * Get Job Details for all the jobs within an Experiment.
           * This method to be used when need to get the job details for one or many jobs of an Experiment.
           *
           * @param authzToken
           *
           * @param experiementId
           *     Experiment ID of the experimnet you need job details.
           *
           * @return list of JobDetails
           *     Job details.
           *
           **/
           list<job_model.JobModel> getJobDetails(1: required string airavataExperimentId)
                         throws (1: registry_api_errors.RegistryServiceException rse,
                                 2: airavata_errors.ExperimentNotFoundException enf)


          /*
           *
           * API definitions for App Catalog related operations
           *
          */

          /**
             *
             * Register a Application Module.
             *
             * @gatewayId
             *    ID of the gateway which is registering the new Application Module.
             *
             * @param applicationModule
             *    Application Module Object created from the datamodel.
             *
             * @return appModuleId
             *   Returns the server-side generated airavata appModule globally unique identifier.
             *
            */
            string registerApplicationModule(1: required string gatewayId,
                                  2: required application_deployment_model.ApplicationModule applicationModule)
              	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch a Application Module.
             *
             * @param appModuleId
             *   The unique identifier of the application module required
             *
             * @return applicationModule
             *   Returns an Application Module Object.
             *
            */
            application_deployment_model.ApplicationModule getApplicationModule(1: required string appModuleId)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
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
            bool updateApplicationModule(1: required string appModuleId,
                      2: required application_deployment_model.ApplicationModule applicationModule)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all Application Module Descriptions.
             *
             * @param gatewayId
             *    ID of the gateway which need to list all available application deployment documentation.
             *
             * @return list
             *    Returns the list of all Application Module Objects.
             *
            */
            list<application_deployment_model.ApplicationModule> getAllAppModules (1: required string gatewayId)
                  throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all Application Module Descriptions.
             *
             * @param gatewayId
             *    ID of the gateway which need to list all available application deployment documentation.
             * @param accessibleAppDeploymentIds
             *    Application Deployment IDs which are accessible to the current user.
             *
             * @return list
             *    Returns the list of all Application Module Objects.
             *
            */
            list<application_deployment_model.ApplicationModule> getAccessibleAppModules (1: required string gatewayId
                    2: required list<string> accessibleAppDeploymentIds,
                    3: required list<string> accessibleComputeResourceIds)
                  throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Delete an Application Module.
             *
             * @param appModuleId
             *   The identifier of the Application Module to be deleted.
             *
             * @return status
             *   Returns a success/failure of the deletion.
             *
            */
            bool deleteApplicationModule(1: required string appModuleId)
                   	throws (1: registry_api_errors.RegistryServiceException rse)

          /*
           *
           * Application Deployment
           *  Registers a deployment of an Application Module on a Compute Resource.
           *
          */

            /**
             *
             * Register an Application Deployment.
             *
             * @param gatewayId
             *    ID of the gateway which is registering the new Application Deployment.
             *
             * @param applicationDeployment
             *    Application Module Object created from the datamodel.
             *
             * @return appDeploymentId
             *   Returns a server-side generated airavata appDeployment globally unique identifier.
             *
            */
            string registerApplicationDeployment(1: required string gatewayId,
                          2: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
              	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch a Application Deployment.
             *
             * @param appDeploymentId
             *   The identifier for the requested application module
             *
             * @return applicationDeployment
             *   Returns a application Deployment Object.
             *
            */
            application_deployment_model.ApplicationDeploymentDescription getApplicationDeployment(1: required string appDeploymentId)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Update an Application Deployment.
             *
             * @param appDeploymentId
             *   The identifier of the requested application deployment to be updated.
             *
             * @param appDeployment
             *    Application Deployment Object created from the datamodel.
             *
             * @return status
             *   Returns a success/failure of the update.
             *
            */
            bool updateApplicationDeployment(1: required string appDeploymentId,
                      2: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Delete an Application Deployment.
             *
             * @param appDeploymentId
             *   The unique identifier of application deployment to be deleted.
             *
             * @return status
             *   Returns a success/failure of the deletion.
             *
            */
            bool deleteApplicationDeployment(1: required string appDeploymentId)
                   	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all Application Deployment Descriptions.
             *
             * @param gatewayId
             *    ID of the gateway which need to list all available application deployment documentation.
             *
             * @return list<applicationDeployment.
             *    Returns the list of all application Deployment Objects.
             *
            */
            list<application_deployment_model.ApplicationDeploymentDescription> getAllApplicationDeployments(1: required string gatewayId)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all Application Deployment Descriptions.
             *
             * @param gatewayId
             *    ID of the gateway which need to list all available application deployment documentation.
             *
             * @param accessibleAppDeploymentIds
             *    Application Deployment IDs which are accessible to the current user.
             *
             * @return list<applicationDeployment>
             *    Returns the list of all application Deployment Objects.
             *
            */
            list<application_deployment_model.ApplicationDeploymentDescription> getAccessibleApplicationDeployments(1: required string gatewayId,
                        2: required list<string> accessibleAppDeploymentIds,
                        3: required list<string> accessibleComputeResourceIds)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all accessible Application Deployment Descriptions for the given Application Module.
             *
             * @param gatewayId
             *    ID of the gateway which need to list all available application deployment documentation.
             *
             * @param appModuleId
             *    The given Application Module ID.
             *
             * @param accessibleAppDeploymentIds
             *    Application Deployment IDs which are accessible to the current user.
             *
             * @param accessibleComputeResourceIds
             *    Compute Resource IDs which are accessible to the current user.
             *
             * @return list<applicationDeployment>
             *    Returns the list of all application Deployment Objects.
             *
             */
            list<application_deployment_model.ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(1: required string gatewayId,
                        2: required string appModuleId,
                        3: required list<string> accessibleAppDeploymentIds,
                        4: required list<string> accessibleComputeResourceIds)
                throws (1: registry_api_errors.RegistryServiceException rse)

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
            list<string> getAppModuleDeployedResources(1: required string appModuleId)
                	throws (1: registry_api_errors.RegistryServiceException rse)


            /**
            *
            * Fetch Application Deployment Description list
            *
            * @param appModuleId
            *   The identifier for the requested application module
            */
            list<application_deployment_model.ApplicationDeploymentDescription> getApplicationDeployments (1: required string appModuleId)
                    throws (1: registry_api_errors.RegistryServiceException rse)

          /*
           *
           * Application Interface
           *
          */

            /**
             *
             * Register a Application Interface.
             *
             * @param applicationInterface
             *    Application Module Object created from the datamodel.
             *
             * @return appInterfaceId
             *   Returns a server-side generated airavata application interface globally unique identifier.
             *
            */
            string registerApplicationInterface(1: required string gatewayId,
                          2: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
              	throws (1: registry_api_errors.RegistryServiceException rse)


         /**
            *
            * Fetch an Application Interface.
            *
            * @param appInterfaceId
            *   The identifier for the requested application interface.
            *
            * @return applicationInterface
            *   Returns an application Interface Object.
            *
           */
           application_interface_model.ApplicationInterfaceDescription getApplicationInterface(1: required string appInterfaceId)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Update a Application Interface.
            *
            * @param appInterfaceId
            *   The identifier of the requested application deployment to be updated.
            *
            * @param appInterface
            *    Application Interface Object created from the datamodel.
            *
            * @return status
            *   Returns a success/failure of the update.
            *
           */
           bool updateApplicationInterface(1: required string appInterfaceId,
                     2: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Delete an Application Interface.
            *
            * @param appInterfaceId
            *   The identifier for the requested application interface to be deleted.
            *
            * @return status
            *   Returns a success/failure of the deletion.
            *
           */
           bool deleteApplicationInterface(1: required string appInterfaceId)
                  	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Fetch name and ID of  Application Interface documents.
            *
            *
            * @return map<applicationId, applicationInterfaceNames>
            *   Returns a list of application interfaces with corresponsing ID's
            *
           */
           map<string, string> getAllApplicationInterfaceNames (1: required string gatewayId)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Fetch all Application Interface documents.
            *
            *
            * @return map<applicationId, applicationInterfaceNames>
            *   Returns a list of application interfaces documents (Application Interface ID, name, description, Inputs and Outputs objects).
            *
           */
           list<application_interface_model.ApplicationInterfaceDescription> getAllApplicationInterfaces (1: required string gatewayId)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Fetch the list of Application Inputs.
            *
            * @param appInterfaceId
            *   The identifier of the application interface which need inputs to be fetched.
            *
            * @return list<application_interface_model.InputDataObjectType>
            *   Returns a list of application inputs.
            *
           */
           list<application_io_models.InputDataObjectType> getApplicationInputs(1: required string appInterfaceId)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Fetch list of Application Outputs.
            *
            * @param appInterfaceId
            *   The identifier of the application interface which need outputs to be fetched.
            *
            * @return list<application_interface_model.OutputDataObjectType>
            *   Returns a list of application outputs.
            *
           */
           list<application_io_models.OutputDataObjectType> getApplicationOutputs(1: required string appInterfaceId)
               	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
            *
            * Fetch a list of all deployed Compute Hosts for a given application interfaces.
            *
            * @param appInterfaceId
            *   The identifier for the requested application interface.
            *
            * @return map<computeResourceId, computeResourceName>
            *   A map of registered compute resource id's and their corresponding hostnames.
            *   Deployments of each modules listed within the interfaces will be listed.
            *
           */
           map<string, string> getAvailableAppInterfaceComputeResources(1: required string appInterfaceId)
               	throws (1: registry_api_errors.RegistryServiceException rse)



          /*
           *
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
            string registerComputeResource(1: required compute_resource_model.ComputeResourceDescription computeResourceDescription)
              	throws (1: registry_api_errors.RegistryServiceException rse)

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
            compute_resource_model.ComputeResourceDescription getComputeResource(1: required string computeResourceId)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Fetch all registered Compute Resources.
             *
             * @return A map of registered compute resource id's and thier corresponding hostnames.
             *    Compute Resource Object created from the datamodel..
             *
            */
            map<string, string> getAllComputeResourceNames()
                	throws (1: registry_api_errors.RegistryServiceException rse)

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

            bool updateComputeResource(1: required string computeResourceId,
                      2: required compute_resource_model.ComputeResourceDescription computeResourceDescription)
                	throws (1: registry_api_errors.RegistryServiceException rse)

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
            bool deleteComputeResource(1: required string computeResourceId)
                   	throws (1: registry_api_errors.RegistryServiceException rse)

          /*
           *
           * Storage Resource
           *
          */

            /**
             * Register a Storage Resource.
             *
             * @param storageResourceDescription
             *    Storge Resource Object created from the datamodel.
             *
             * @return storageResourceId
             *   Returns a server-side generated airavata storage resource globally unique identifier.
             *
            */

            string registerStorageResource(1: required storage_resource_model.StorageResourceDescription storageResourceDescription)
              	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             * Fetch the given Storage Resource.
             *
             * @param storageResourceId
             *   The identifier for the requested storage resource
             *
             * @return storageResourceDescription
             *    Storage Resource Object created from the datamodel..
             *
            */

            storage_resource_model.StorageResourceDescription getStorageResource(1: required string storageResourceId)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             * Fetch all registered Storage Resources.
             *
             * @return A map of registered compute resource id's and thier corresponding hostnames.
             *    Compute Resource Object created from the datamodel..
             *
            */

            map<string, string> getAllStorageResourceNames()
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             * Update a Storage Resource.
             *
             * @param storageResourceId
             *   The identifier for the requested compute resource to be updated.
             *
             * @param storageResourceDescription
             *    Storage Resource Object created from the datamodel.
             *
             * @return status
             *   Returns a success/failure of the update.
             *
            */

            bool updateStorageResource(1: required string storageResourceId,
                      2: required storage_resource_model.StorageResourceDescription storageResourceDescription)
                	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             * Delete a Storage Resource.
             *
             * @param storageResourceId
             *   The identifier of the requested compute resource to be deleted.
             *
             * @return status
             *   Returns a success/failure of the deletion.
             *
            */

            bool deleteStorageResource(1: required string storageResourceId)
                   	throws (1: registry_api_errors.RegistryServiceException rse)

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

            string addLocalSubmissionDetails(1: required string computeResourceId,
                      2: required i32 priorityOrder,
                      3: required compute_resource_model.LOCALSubmission localSubmission)

            	throws (1: registry_api_errors.RegistryServiceException rse)

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

            bool updateLocalSubmissionDetails(1: required string jobSubmissionInterfaceId,
                      2: required compute_resource_model.LOCALSubmission localSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
            * This method returns localJobSubmission object
            * @param jobSubmissionInterfaceId
            *   The identifier of the JobSubmission Interface to be retrieved.
            *  @return LOCALSubmission instance
            **/

            compute_resource_model.LOCALSubmission getLocalJobSubmission(1: required string jobSubmissionId)
                     throws (1: registry_api_errors.RegistryServiceException rse)



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

            string addSSHJobSubmissionDetails(1: required string computeResourceId,
                      2: required i32 priorityOrder,
                      3: required compute_resource_model.SSHJobSubmission sshJobSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

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


            string addSSHForkJobSubmissionDetails(1: required string computeResourceId,
                      2: required i32 priorityOrder,
                      3: required compute_resource_model.SSHJobSubmission sshJobSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

              /**
              * This method returns SSHJobSubmission object
              * @param jobSubmissionInterfaceId
              *   The identifier of the JobSubmission Interface to be retrieved.
              *  @return SSHJobSubmission instance
              **/

              compute_resource_model.SSHJobSubmission getSSHJobSubmission(1: required string jobSubmissionId)
                        throws (1: registry_api_errors.RegistryServiceException rse)



            /**
             *
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
            string addUNICOREJobSubmissionDetails(1: required string computeResourceId,
                      2: required i32 priorityOrder,
                      3: required compute_resource_model.UnicoreJobSubmission unicoreJobSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)


            /**
              *
              * This method returns UnicoreJobSubmission object
              *
              * @param jobSubmissionInterfaceId
              *   The identifier of the JobSubmission Interface to be retrieved.
              *  @return UnicoreJobSubmission instance
              *
            **/
            compute_resource_model.UnicoreJobSubmission getUnicoreJobSubmission(1: required string jobSubmissionId)
                         throws (1: registry_api_errors.RegistryServiceException rse)


           /**
             *
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
             *
          **/
           string addCloudJobSubmissionDetails(1: required string computeResourceId,
                      2: required i32 priorityOrder,
                      3: required compute_resource_model.CloudJobSubmission cloudSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

           /**
              *
              * This method returns cloudJobSubmission object
              * @param jobSubmissionInterfaceI
                  *   The identifier of the JobSubmission Interface to be retrieved.
              *  @return CloudJobSubmission instance
           **/
           compute_resource_model.CloudJobSubmission getCloudJobSubmission(1: required string jobSubmissionId)
                            throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Update the given SSH Job Submission details
             *
             * @param jobSubmissionInterfaceId
             *   The identifier of the JobSubmission Interface to be updated.
             *
             * @param sshJobSubmission
             *   The SSHJobSubmission object to be updated.
             *
             * @return status
             *   Returns a success/failure of the update.
             *
            */
            bool updateSSHJobSubmissionDetails(1: required string jobSubmissionInterfaceId,
                      2: required compute_resource_model.SSHJobSubmission sshJobSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

          /**
             *
             * Update the cloud Job Submission details
             *
             * @param jobSubmissionInterfaceId
             *   The identifier of the JobSubmission Interface to be updated.
             *
             * @param cloudJobSubmission
             *   The CloudJobSubmission object to be updated.
             *
             * @return status
             *   Returns a success/failure of the update.
             *
            */
            bool updateCloudJobSubmissionDetails(1: required string jobSubmissionInterfaceId,
                      2: required compute_resource_model.CloudJobSubmission sshJobSubmission)
            	throws (1: registry_api_errors.RegistryServiceException rse)

             /**
             *
             * Update the UNIOCRE Job Submission details
             *
             * @param jobSubmissionInterfaceId
             *   The identifier of the JobSubmission Interface to be updated.
             *
             * @param UnicoreJobSubmission
             *   The UnicoreJobSubmission object to be updated.
             *
             * @return status
             *   Returns a success/failure of the update.
             *
             **/
            bool updateUnicoreJobSubmissionDetails(1: required string jobSubmissionInterfaceId,
                        2: required compute_resource_model.UnicoreJobSubmission unicoreJobSubmission)
              	throws (1: registry_api_errors.RegistryServiceException rse)

             /**
             *
             * Add a Local data movement details to a compute resource
             *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
             *
             * @param productUri
             *   The identifier of the compute resource to which JobSubmission protocol to be added
             *
             * @param DMType
             *   DMType object to be added to the resource.
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
             **/
            string addLocalDataMovementDetails(1: required string productUri,
                      2: required data_movement_models.DMType dataMoveType,
                      3: required i32 priorityOrder,
                      4: required data_movement_models.LOCALDataMovement localDataMovement)
            	throws (1: registry_api_errors.RegistryServiceException rse)

             /**
             *
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
             **/
            bool updateLocalDataMovementDetails(1: required string dataMovementInterfaceId,
                      2: required data_movement_models.LOCALDataMovement localDataMovement)
            	throws (1: registry_api_errors.RegistryServiceException rse)

            /**
            *
            * This method returns local datamovement object.
            *
            * @param dataMovementId
            *   The identifier of the datamovement Interface to be retrieved.
            *
            *  @return LOCALDataMovement instance
            *
            **/
            data_movement_models.LOCALDataMovement getLocalDataMovement(1: required string dataMovementId)
                              throws (1: registry_api_errors.RegistryServiceException rse)

            /**
             *
             * Add a SCP data movement details to a compute resource
             *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
             *
             * @param productUri
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
            string addSCPDataMovementDetails(1: required string productUri,
                      2: required data_movement_models.DMType dataMoveType,
                      3: required i32 priorityOrder,
                      4: required data_movement_models.SCPDataMovement scpDataMovement)
            	throws (1: registry_api_errors.RegistryServiceException rse)


            /**
               *
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
              bool updateSCPDataMovementDetails(1: required string dataMovementInterfaceId,
                        2: required data_movement_models.SCPDataMovement scpDataMovement)
                throws (1: registry_api_errors.RegistryServiceException rse)

                /**
                * This method returns SCP datamovement object
                *
                * @param dataMovementId
                *   The identifier of the datamovement Interface to be retrieved.
                *
                * @return SCPDataMovement instance
                *
                **/
              data_movement_models.SCPDataMovement getSCPDataMovement(1: required string dataMovementId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

             /**
               *
               * Add a UNICORE data movement details to a compute resource
               *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
               *
               * @param productUri
               *   The identifier of the compute resource to which data movement protocol to be added
               *
               * @param priorityOrder
               *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
               *
               * @param UnicoreDataMovement
               *   The UnicoreDataMovement object to be added to the resource.
               *
               * @return status
               *   Returns the unique data movement id.
               *
              */
             string addUnicoreDataMovementDetails(1: required string productUri,
                          2: required data_movement_models.DMType dataMoveType,
                          3: required i32 priorityOrder,
                          4: required data_movement_models.UnicoreDataMovement unicoreDataMovement)
                    throws (1: registry_api_errors.RegistryServiceException rse)

               /**
               *
               * Update a selected UNICORE data movement details
               *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
               *
               * @param dataMovementInterfaceId
               *   The identifier of the data movement Interface to be updated.
               *
               * @param UnicoreDataMovement
               *   The UnicoreDataMovement object to be updated.
               *
               * @return status
               *   Returns a success/failure of the update.
               *
               **/

             bool updateUnicoreDataMovementDetails(1: required string dataMovementInterfaceId,
                         2: required data_movement_models.UnicoreDataMovement unicoreDataMovement)
                throws (1: registry_api_errors.RegistryServiceException rse)

                /**
                *
                * This method returns UNICORE datamovement object
                *
                * @param dataMovementId
                *   The identifier of the datamovement Interface to be retrieved.
                *
                * @return UnicoreDataMovement instance
                *
                **/

             data_movement_models.UnicoreDataMovement getUnicoreDataMovement(1: required string dataMovementId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               /**
               *
               * Add a GridFTP data movement details to a compute resource
               *  App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
               *
               * @param productUri
               *   The identifier of the compute resource to which dataMovement protocol to be added
               *
               * @param DMType
               *    The DMType object to be added to the resource.
               *
               * @param priorityOrder
               *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
               *
               * @param gridFTPDataMovement
               *   The GridFTPDataMovement object to be added to the resource.
               *
               * @return status
               *   Returns the unique data movement id.
               *
               **/

              string addGridFTPDataMovementDetails(1: required string productUri,
                        2: required data_movement_models.DMType dataMoveType,
                        3: required i32 priorityOrder,
                        4: required data_movement_models.GridFTPDataMovement gridFTPDataMovement)
                throws (1: registry_api_errors.RegistryServiceException rse)

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
               * @return boolean
               *   Returns a success/failure of the update.
               *
               **/

              bool updateGridFTPDataMovementDetails(1: required string dataMovementInterfaceId,
                        2: required data_movement_models.GridFTPDataMovement gridFTPDataMovement)
               throws (1: registry_api_errors.RegistryServiceException rse)

                /**
                * This method returns GridFTP datamovement object
                *
                * @param dataMovementId
                *   The identifier of the datamovement Interface to be retrieved.
                *
                *  @return GridFTPDataMovement instance
                *
                **/

              data_movement_models.GridFTPDataMovement getGridFTPDataMovement(1: required string dataMovementId)
                                throws (1: registry_api_errors.RegistryServiceException rse)


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
               **/

              bool changeJobSubmissionPriority(1: required string jobSubmissionInterfaceId,
                        2: required i32 newPriorityOrder)
                throws (1: registry_api_errors.RegistryServiceException rse)

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
               **/

              bool changeDataMovementPriority(1: required string dataMovementInterfaceId,
                        2: required i32 newPriorityOrder)
                throws (1: registry_api_errors.RegistryServiceException rse)

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

              bool changeJobSubmissionPriorities(1: required map<string, i32> jobSubmissionPriorityMap)
                throws (1: registry_api_errors.RegistryServiceException rse)

               /**
               * Change the priorities of a given set of data movement interfaces
               *
               * @param dataMovementPriorityMap
               *   A Map of identifiers of the DataMovement Interfaces and thier associated priorities to be set.
               *
               * @return status
               *   Returns a success/failure of the changes.
               *
               **/

              bool changeDataMovementPriorities(1: required map<string, i32> dataMovementPriorityMap)
                throws (1: registry_api_errors.RegistryServiceException rse)

               /**
               * Delete a given job submisison interface
               *
               * @param jobSubmissionInterfaceId
               *   The identifier of the JobSubmission Interface to be changed
               *
               * @return status
               *   Returns a success/failure of the deletion.
               *
               **/

              bool deleteJobSubmissionInterface(1: required string computeResourceId,
                        2: required string jobSubmissionInterfaceId)
                throws (1: registry_api_errors.RegistryServiceException rse)

               /**
               * Delete a given data movement interface
               *
               * @param dataMovementInterfaceId
               *   The identifier of the DataMovement Interface to be changed
               *
               * @return status
               *   Returns a success/failure of the deletion.
               *
               **/

              bool deleteDataMovementInterface(1: required string productUri,
                        2: required string dataMovementInterfaceId,
                        3: required data_movement_models.DMType dataMoveType,)
                throws (1: registry_api_errors.RegistryServiceException rse)

             string registerResourceJobManager(1: required compute_resource_model.ResourceJobManager resourceJobManager)
                throws (1: registry_api_errors.RegistryServiceException rse)

             bool updateResourceJobManager(1: required string resourceJobManagerId,
                        2: required compute_resource_model.ResourceJobManager updatedResourceJobManager)
                 throws (1: registry_api_errors.RegistryServiceException rse)

             compute_resource_model.ResourceJobManager getResourceJobManager(1: required string resourceJobManagerId)
                  throws (1: registry_api_errors.RegistryServiceException rse)

             bool deleteResourceJobManager(1: required string resourceJobManagerId)
                   throws (1: registry_api_errors.RegistryServiceException rse)

              /**
               * Delete a Compute Resource Queue
               *
               * @param computeResourceId
               *   The identifier of the compute resource which has the queue to be deleted
               *
               * @param queueName
               *   Name of the queue need to be deleted. Name is the uniqueue identifier for the queue within a compute resource
               *
               * @return status
               *   Returns a success/failure of the deletion.
               *
               **/

              bool deleteBatchQueue(1: required string computeResourceId, 2: required string queueName)
                    throws (1: registry_api_errors.RegistryServiceException rse)
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
              string registerGatewayResourceProfile(1: required gateway_resource_profile_model.GatewayResourceProfile gatewayResourceProfile)
                    throws (1: registry_api_errors.RegistryServiceException rse)

              /**
               * Fetch the given Gateway Resource Profile.
               *
               * @param gatewayID
               *   The identifier for the requested gateway resource.
               *
               * @return gatewayResourceProfile
               *    Gateway Resource Profile Object.
               *
              */

              gateway_resource_profile_model.GatewayResourceProfile getGatewayResourceProfile(1: required string gatewayID)
                    throws (1: registry_api_errors.RegistryServiceException rse)

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

              bool updateGatewayResourceProfile(1: required string gatewayID,
                        2: required gateway_resource_profile_model.GatewayResourceProfile gatewayResourceProfile)
                    throws (1: registry_api_errors.RegistryServiceException rse)

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

              bool deleteGatewayResourceProfile(1: required string gatewayID)
                        throws (1: registry_api_errors.RegistryServiceException rse)

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
              bool addGatewayComputeResourcePreference(1: required string gatewayID,
                        2: required string computeResourceId,
                        3: required gateway_resource_profile_model.ComputeResourcePreference computeResourcePreference)
                throws (1: registry_api_errors.RegistryServiceException rse)

              /**
               * Add a Storage Resource Preference to a registered gateway profile.
               *
               * @param gatewayID
               *   The identifier of the gateway profile to be added.
               *
               * @param storageResourceId
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
              bool addGatewayStoragePreference(1: required string gatewayID,
                          2: required string storageResourceId,
                          3: required gateway_resource_profile_model.StoragePreference storagePreference)
                    throws (1: registry_api_errors.RegistryServiceException rse)
              /**
               *
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
              gateway_resource_profile_model.ComputeResourcePreference getGatewayComputeResourcePreference(1: required string gatewayID,
                        2: required string computeResourceId)
                throws (1: registry_api_errors.RegistryServiceException rse)

              /**
               *
               * Fetch a Storage Resource Preference of a registered gateway profile.
               *
               * @param gatewayID
               *   The identifier of the gateway profile to request to fetch the particular storage resource preference.
               *
               * @param storageResourceId
               *   Identifier of the Stprage Preference required to be fetched.
               *
               * @return StoragePreference
               *   Returns the StoragePreference object.
               *
              */
              gateway_resource_profile_model.StoragePreference getGatewayStoragePreference(1: required string gatewayID,
                          2: required string storageResourceId)
                    throws (1: registry_api_errors.RegistryServiceException rse)

              /**
               *
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
                        getAllGatewayComputeResourcePreferences(1: required string gatewayID)
                throws (1: registry_api_errors.RegistryServiceException rse)

              /**
              * Fetch all Storage Resource Preferences of a registered gateway profile.
              *
              * @param gatewayID
              *   The identifier for the gateway profile to be requested
              *
              * @return StoragePreference
              *   Returns the StoragePreference object.
              *
             */

              list<gateway_resource_profile_model.StoragePreference>
                          getAllGatewayStoragePreferences(1: required string gatewayID)
                    throws (1: registry_api_errors.RegistryServiceException rse)

              /**
              *
              * Fetch all Gateway Profiles registered
              *
              * @return GatewayResourceProfile
              *   Returns all the GatewayResourcePrifle list object.
              *
              **/
              list<gateway_resource_profile_model.GatewayResourceProfile>
                          getAllGatewayResourceProfiles()
                    throws (1: registry_api_errors.RegistryServiceException rse)

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
              bool updateGatewayComputeResourcePreference(1: required string gatewayID,
                        2: required string computeResourceId,
                        3: required gateway_resource_profile_model.ComputeResourcePreference computeResourcePreference)
                throws (1: registry_api_errors.RegistryServiceException rse)

                /**
                 * Update a Storage Resource Preference of a registered gateway profile.
                 *
                 * @param gatewayID
                 *   The identifier of the gateway profile to be updated.
                 *
                 * @param storageId
                 *   The Storage resource identifier of the one that you want to update
                 *
                 * @param storagePreference
                 *   The storagePreference object to be updated to the resource profile.
                 *
                 * @return status
                 *   Returns a success/failure of the updation.
                 *
                */

              bool updateGatewayStoragePreference(1: required string gatewayID,
                          2: required string storageId,
                          3: required gateway_resource_profile_model.StoragePreference storagePreference)
                    throws (1: registry_api_errors.RegistryServiceException rse)

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
              bool deleteGatewayComputeResourcePreference(1: required string gatewayID,
                        2: required string computeResourceId)
                throws (1: registry_api_errors.RegistryServiceException rse)


              /**
               * Delete the Storage Resource Preference of a registered gateway profile.
               *
               * @param gatewayID
               *   The identifier of the gateway profile to be deleted.
               *
               * @param storageId
               *   ID of the storage preference you want to delete.
               *
               * @return status
               *   Returns a success/failure of the deletion.
               *
              */

              bool deleteGatewayStoragePreference(1: required string gatewayID,
                          2: required string storageId)
                    throws (1: registry_api_errors.RegistryServiceException rse)

             /**
                * Delete the Storage Resource Preference of a registered gateway profile.
                *
                * @param gatewayID
                *   The identifier of the gateway profile to be deleted.
                *
                * @param storageId
                *   ID of the storage preference you want to delete.
                *
                * @return status
                *   Returns a success/failure of the deletion.
                *
               */


               /*
               * User Resource Profile
               *
               */

               /**
                * Register a User Resource Profile.
                *
                * @param userResourceProfile
                *    User Resource Profile Object.
                *    The userId should be obtained from Airavata user profile and passed to register a corresponding
                *      resource profile.
                *
                * @return status
                *   Returns a success/failure of the update.
                *
               */
               string registerUserResourceProfile(1: required user_resource_profile_model.UserResourceProfile userResourceProfile)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Check if the given User Resource Profile exists.
                *
                * @param userId
                *   The identifier for the requested User Resource Profile.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource Profile.
                *
                * @return true if User Resource Profile for these identifiers exists.
                *
               */
               bool isUserResourceProfileExists(1: required string userId, 2: required string gatewayID)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Fetch the given User Resource Resource Profile.
                *
                * @param userId
                *   The identifier for the requested User Resource resource.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @return userResourceProfile or null if not found
                *    User Resource Profile Object.
                *
               */

               user_resource_profile_model.UserResourceProfile getUserResourceProfile(1: required string userId, 2: required string gatewayID)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Update a User Resource Profile.
                *
                * @param userId
                *   The identifier for the requested user resource to be updated.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userResourceProfile
                *    User Resource Profile Object.
                *
                * @return status
                *   Returns a success/failure of the update.
                *
               */

               bool updateUserResourceProfile(1: required string userId, 2: required string gatewayID,
                         3: required user_resource_profile_model.UserResourceProfile userResourceProfile)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Delete the given User Resource Profile.
                *
                * @param userId
                *   The identifier for the requested user resource to be deleted.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @return status
                *   Returns a success/failure of the deletion.
                *
               */

               bool deleteUserResourceProfile(1: required string userId, 2: required string gatewayID)
                         throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Adds a new User Profile.
                *
                * @param userProfile
                *   The user profile to add.
                *
                * @return userId
                *   Returns the userId of the user profile added.
                *
               */
               string addUser(1: required user_profile_model.UserProfile userProfile)
                        throws (1: registry_api_errors.RegistryServiceException rse, 2: airavata_errors.DuplicateEntryException dee)

               /**
                * Add a Compute Resource Preference to a registered user resource profile.
                *
                * @param userId
                *   The identifier for the user resource profile to be added.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
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
               bool addUserComputeResourcePreference(1: required string userId,
                         2: required string gatewayID,
                         3: required string computeResourceId,
                         4: required user_resource_profile_model.UserComputeResourcePreference userComputeResourcePreference)
                 throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Add a Storage Resource Preference to a registered user profile.
                *
                * @param userId
                *   The identifier of the userId profile to be added.
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userStorageResourceId
                *   Preferences related to a particular compute resource
                *
                * @param userStoragePreference
                *   The ComputeResourcePreference object to be added to the resource profile.
                *
                * @return status
                *   Returns a success/failure of the addition. If a profile already exists, this operation will fail.
                *    Instead an update should be used.
                *
               */
               bool addUserStoragePreference(1: required string userId,
                           2: required string gatewayID,
                           3: required string userStorageResourceId,
                           4: required user_resource_profile_model.UserStoragePreference userStoragePreference)
                     throws (1: registry_api_errors.RegistryServiceException rse)
               /**
                *
                * Fetch a Compute Resource Preference of a registered user resource profile.
                *
                * @param userId
                *   The identifier for the user resource profile to be requested
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userComputeResourceId
                *   Preferences related to a particular compute resource
                *
                * @return userComputeResourcePreference
                *   Returns the ComputeResourcePreference object.
                *
               */
               user_resource_profile_model.UserComputeResourcePreference getUserComputeResourcePreference(1: required string userId,
                         2: required string gatewayID,
                         3: required string userComputeResourceId)
                 throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                *
                * Fetch a Storage Resource Preference of a registered user profile.
                *
                * @param userId
                *   The identifier of the user resource profile to request to fetch the particular storage resource preference.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userStorageResourceId
                *   Identifier of the Stprage Preference required to be fetched.
                *
                * @return UserStoragePreference
                *   Returns the StoragePreference object.
                *
               */
               user_resource_profile_model.UserStoragePreference getUserStoragePreference(1: required string userId,
                           2: required string gatewayID,
                           3: required string userStorageResourceId)
                     throws (1: registry_api_errors.RegistryServiceException rse)

                            /**
                              *
                              * Fetch all User Compute Resource Preferences of a registered user resource profile.
                              *
                              * @param userId
                              *
                              * @param gatewayID
                              *   The identifier for the gateway profile to be requested
                              *
                              * @return computeResourcePreference
                              *   Returns the ComputeResourcePreference object.
                              *
                             */
                             list<user_resource_profile_model.UserComputeResourcePreference>
                                       getAllUserComputeResourcePreferences(1: required string userId,
                                       2: required string gatewayID)
                               throws (1: registry_api_errors.RegistryServiceException rse)

                             /**
                             * Fetch all User Storage Resource Preferences of a registered user resource profile.
                             *
                             * @param gatewayID
                             *   The identifier for the gateway profile to be requested
                             *
                             * @return StoragePreference
                             *   Returns the StoragePreference object.
                             *
                            */

                             list<user_resource_profile_model.UserStoragePreference>
                                         getAllUserStoragePreferences(1: required string userId,
                                         2: required string gatewayID)
                                   throws (1: registry_api_errors.RegistryServiceException rse)


               /**
               *
               * Fetch all user resource Profiles registered
               *
               * @return userResourceProfile
               *   Returns all the UserResourcePrifle list object.
               *
               **/
               list<user_resource_profile_model.UserResourceProfile>
                           getAllUserResourceProfiles()
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Update a Compute Resource Preference to a registered user resource profile.
                *
                * @param userId
                *   The identifier for the user resource profile to be updated.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userComputeResourceId
                *   Preferences related to a particular compute resource
                *
                * @param userComputeResourcePreference
                *   The ComputeResourcePreference object to be updated to the resource profile.
                *
                * @return status
                *   Returns a success/failure of the updation.
                *
               */
               bool updateUserComputeResourcePreference(1: required string userId,
                         2: required string gatewayID,
                         3: required string userComputeResourceId,
                         4: required user_resource_profile_model.UserComputeResourcePreference userComputeResourcePreference)
                 throws (1: registry_api_errors.RegistryServiceException rse)

                 /**
                  * Update a Storage Resource Preference of a registered user profile.
                  *
                  * @param userId
                  *   The identifier of the user resource profile to be updated.
                  *
                  * @param gatewayID
                  *   The identifier to link gateway for the requested User Resource resource.
                  *
                  * @param userStorageId
                  *   The Storage resource identifier of the one that you want to update
                  *
                  * @param userStoragePreference
                  *   The storagePreference object to be updated to the resource profile.
                  *
                  * @return status
                  *   Returns a success/failure of the updation.
                  *
                 */

               bool updateUserStoragePreference(1: required string userId,
                           2: required string gatewayID,
                           3: required string userStorageId,
                           4: required user_resource_profile_model.UserStoragePreference userStoragePreference)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Delete the Compute Resource Preference of a registered user resource profile.
                *
                * @param userId
                *   The identifier for the user resource profile to be deleted.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userComputeResourceId
                *   Preferences related to a particular compute resource
                *
                * @return status
                *   Returns a success/failure of the deletion.
                *
               */
               bool deleteUserComputeResourcePreference(1: required string userId,
                         2: required string gatewayID,
                         3: required string userComputeResourceId)
                 throws (1: registry_api_errors.RegistryServiceException rse)


               /**
                * Delete the Storage Resource Preference of a registered user resource profile.
                *
                * @param userId
                *   The identifier of the user resource profile to be deleted.
                *
                * @param gatewayID
                *   The identifier to link gateway for the requested User Resource resource.
                *
                * @param userStorageId
                *   ID of the storage preference you want to delete.
                *
                * @return status
                *   Returns a success/failure of the deletion.
                *
               */

               bool deleteUserStoragePreference(1: required string userId,
                           2: required string gatewayID,
                           3: required string userStorageId)
                     throws (1: registry_api_errors.RegistryServiceException rse)

               /**
                * Get queue statuses of all compute resources
               **/
               list<status_models.QueueStatusModel> getLatestQueueStatuses()
                     throws (1: registry_api_errors.RegistryServiceException rse)

               void registerQueueStatuses(1: required list<status_models.QueueStatusModel> queueStatuses)
                                    throws (1: registry_api_errors.RegistryServiceException rse)

              /**
              * API Methods related to replica catalog
              **/
              string registerDataProduct(1: required  replica_catalog_models.DataProductModel dataProductModel)
                        throws (1: registry_api_errors.RegistryServiceException rse)

              replica_catalog_models.DataProductModel getDataProduct(1: required  string dataProductUri)
                         throws (1: registry_api_errors.RegistryServiceException rse)

              string registerReplicaLocation(1: required  replica_catalog_models.DataReplicaLocationModel replicaLocationModel)
                           throws (1: registry_api_errors.RegistryServiceException rse)

              replica_catalog_models.DataProductModel getParentDataProduct(1: required  string productUri)
                           throws (1: registry_api_errors.RegistryServiceException rse)

              list<replica_catalog_models.DataProductModel> getChildDataProducts(1: required  string productUri)
                            throws (1: registry_api_errors.RegistryServiceException rse)

              list<replica_catalog_models.DataProductModel> searchDataProductsByName(1: required  string gatewayId,
               2: required string userId, 3: required string productName, 4: required i32 limit, 5: required i32 offset)
                                          throws (1: registry_api_errors.RegistryServiceException rse)


              /*
               * Group Resource Profile API methods
               *
               */
               string createGroupResourceProfile(1: required group_resource_profile_model.GroupResourceProfile groupResourceProfile)
                        throws (1: registry_api_errors.RegistryServiceException rse)

               void updateGroupResourceProfile(1: required group_resource_profile_model.GroupResourceProfile groupResourceProfile)
                                          throws (1: registry_api_errors.RegistryServiceException rse)

               group_resource_profile_model.GroupResourceProfile getGroupResourceProfile(1: required string groupResourceProfileId)
                        throws (1: registry_api_errors.RegistryServiceException rse)

               bool removeGroupResourceProfile(1: required string groupResourceProfileId)
                                    throws (1: registry_api_errors.RegistryServiceException rse)

               list<group_resource_profile_model.GroupResourceProfile> getGroupResourceList(1: required string gatewayId, 2: required list<string> accessibleGroupResProfileIds)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               bool removeGroupComputePrefs(1: required string computeResourceId, 2: required string groupResourceProfileId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               bool removeGroupComputeResourcePolicy(1: required string resourcePolicyId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               bool removeGroupBatchQueueResourcePolicy(1: required string resourcePolicyId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               group_resource_profile_model.GroupComputeResourcePreference getGroupComputeResourcePreference(1: required string computeResourceId, 2: required string groupResourceProfileId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               group_resource_profile_model.ComputeResourcePolicy getGroupComputeResourcePolicy(1: required string resourcePolicyId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               group_resource_profile_model.BatchQueueResourcePolicy getBatchQueueResourcePolicy(1: required string resourcePolicyId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               list<group_resource_profile_model.GroupComputeResourcePreference> getGroupComputeResourcePrefList( 1: required string groupResourceProfileId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               list<group_resource_profile_model.BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(1: required string groupResourceProfileId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

               list<group_resource_profile_model.ComputeResourcePolicy> getGroupComputeResourcePolicyList(1: required string groupResourceProfileId)
                                throws (1: registry_api_errors.RegistryServiceException rse)

    /*
     * Gateway Groups API methods
     */
    void createGatewayGroups(1: required gateway_groups_model.GatewayGroups gatewayGroups)
            throws (1: registry_api_errors.RegistryServiceException rse, 2: airavata_errors.DuplicateEntryException dee)
    void updateGatewayGroups(1: required gateway_groups_model.GatewayGroups gatewayGroups)
            throws (1: registry_api_errors.RegistryServiceException rse)
    bool isGatewayGroupsExists(1: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse)
    gateway_groups_model.GatewayGroups getGatewayGroups(1: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse)

    parser_model.Parser getParser(1: required string parserId, 2: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    string saveParser(1: required parser_model.Parser parser)
            throws (1: registry_api_errors.RegistryServiceException rse);
    list<parser_model.Parser> listAllParsers(1: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    void removeParser(1: required string parserId, 2: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    parser_model.ParserInput getParserInput(1: required string parserInputId, 2: required string gatewayId)
                throws (1: registry_api_errors.RegistryServiceException rse);
    parser_model.ParserOutput getParserOutput(1: required string parserOutputId, 2: required string gatewayId)
                    throws (1: registry_api_errors.RegistryServiceException rse);

    parser_model.ParsingTemplate getParsingTemplate(1: required string templateId, 2: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    list<parser_model.ParsingTemplate> getParsingTemplatesForExperiment(1: required string experimentId, 2: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    string saveParsingTemplate(1: required parser_model.ParsingTemplate parsingTemplate)
            throws (1: registry_api_errors.RegistryServiceException rse);
    list<parser_model.ParsingTemplate> listAllParsingTemplates(1: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    void removeParsingTemplate(1: required string templateId, 2: required string gatewayId)
            throws (1: registry_api_errors.RegistryServiceException rse);

    bool isGatewayUsageReportingAvailable(1: required string gatewayId, 2: required string computeResourceId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    workspace_model.GatewayUsageReportingCommand getGatewayReportingCommand(1: required string gatewayId, 2: required string computeResourceId)
            throws (1: registry_api_errors.RegistryServiceException rse);
    void addGatewayUsageReportingCommand(1: workspace_model.GatewayUsageReportingCommand command)
            throws (1: registry_api_errors.RegistryServiceException rse);
    void removeGatewayUsageReportingCommand(1: required string gatewayId, 2: required string computeResourceId)
            throws (1: registry_api_errors.RegistryServiceException rse);
}
