/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.api.server.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavataAPIConstants;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.ExperimentNotFoundException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.model.experiment.BasicMetadata;
import org.apache.airavata.model.experiment.ConfigurationData;
import org.apache.thrift.TException;

import java.util.UUID;

public class MockAiravataServerHandler implements Airavata.Iface{
    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String GetAPIVersion() throws TException {
        return airavataAPIConstants.AIRAVATA_API_VERSION;
    }

    /**
     * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * but inferred from the authentication header. This experiment is just a persistent place holder. The client
     * has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * registering the experiment in a persistent store.
     *
     * @param basicExperimentMetadata The create experiment will require the basic experiment metadata like the name and description, intended user,
     *                                the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
     *                                the ExperimentMetadata is a required field.
     * @return The server-side generated airavata experiment globally unique identifier.
     * @throws org.apache.airavata.api.error.InvalidRequestException For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.AiravataClientException The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                               <p/>
     *                                                               UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                               step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                               gateway registration steps and retry this request.
     *                                                               <p/>
     *                                                               AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                               For now this is a place holder.
     *                                                               <p/>
     *                                                               INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                               is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                               rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public String createExperiment(BasicMetadata basicExperimentMetadata) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        //FIXME: this should be generated at the registry CPI level.
        return UUID.randomUUID().toString();
    }

    /**
     * Fetch previously created experiment metadata.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public BasicMetadata getBasicExperimentMetadata(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        return null;
    }

    /**
     * Configure a previously created experiment with required inputs, scheduling and other quality of service
     * parameters. This method only updates the experiment object within the registry. The experiment has to be launched
     * to make it actionable by the server.
     *
     * @param airavataExperimentId        The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experimentConfigurationData The configuration information of the experiment with application input parameters, computational resource scheduling
     *                                    information, special input output handling and additional quality of service parameters.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void configureExperiment(String airavataExperimentId, ConfigurationData experimentConfigurationData) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {

    }

    /**
     * Fetch the previously configured experiment configuration information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method returns the previously configured experiment configuration data.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public ConfigurationData getExperimentConfiguration(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        return null;
    }

    /**
     * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
     * notifications and intermediate and output data will be subsequently available for this experiment.
     *
     * @param airavataExperimentId   The identifier for the requested experiment. This is returned during the create experiment step.
     * @param airavataCredStoreToken :
     *                               A requirement to execute experiments within Airavata is to first register the targeted remote computational account
     *                               credentials with Airavata Credential Store. The administrative API (related to credential store) will return a
     *                               generated token associated with the registered credentials. The client has to security posses this token id and is
     *                               required to pass it to Airavata Server for all execution requests.
     *                               Note: At this point only the credential store token is required so the string is directly passed here. In future if
     *                               if more security credentials are enables, then the structure ExecutionSecurityParameters should be used.
     *                               Note: This parameter is not persisted within Airavata Registry for security reasons.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void launchConfiguredExperiment(String airavataExperimentId, String airavataCredStoreToken) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {

    }

    /**
     * Configure and Launch a previously created experiment with required inputs, scheduling, security and other quality of service
     * parameters. This method also launches the experiment after it is configured. If you would like to configure only
     * and launch at a later time or partially configure then ConfigureExperiment should be used.
     *
     * @param airavataExperimentId        The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experimentConfigurationData The configuration information of the experiment with application input parameters, computational resource scheduling
     *                                    information, special input output handling and additional quality of service parameters.
     * @param airavataCredStoreToken      :
     *                                    A requirement to execute experiments within Airavata is to first register the targeted remote computational account
     *                                    credentials with Airavata Credential Store. The administrative API (related to credential store) will return a
     *                                    generated token associated with the registered credentials. The client has to security posses this token id and is
     *                                    required to pass it to Airavata Server for all execution requests.
     *                                    Note: At this point only the credential store token is required so the string is directly passed here. In future if
     *                                    if more security credentials are enables, then the structure ExecutionSecurityParameters should be used.
     * @return The server-side generated experiment GUID.
     * @throws org.apache.airavata.api.error.InvalidRequestException For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.AiravataClientException The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                               <p/>
     *                                                               UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                               step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                               gateway registration steps and retry this request.
     *                                                               <p/>
     *                                                               AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                               For now this is a place holder.
     *                                                               <p/>
     *                                                               INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                               is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                               rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public String configureAndLaunchExperiment(String airavataExperimentId, ConfigurationData experimentConfigurationData, String airavataCredStoreToken) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        return null;
    }

    /**
     * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
     * The client has to subsequently update this configuration if needed and launch the cloned experiment.
     *
     * @param airavataExperimentIdToBeCloned This is the experiment identifier that is to be cloned.
     * @param basicExperimentMetadata        Once an experiment is cloned, to disambiguate, the users are suggested to provide new metadata. This will again require
     *                                       the basic experiment metadata like the name and description, intended user, the gateway identifier and if the experiment
     *                                       should be shared public by default.
     * @return The server-side generated airavata experiment globally unique identifier for the newly cloned experiment.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public String cloneExperimentConfiguration(String airavataExperimentIdToBeCloned, BasicMetadata basicExperimentMetadata) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        return null;
    }

    /**
     * Terminate a running experiment.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void terminateExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {

    }
}
