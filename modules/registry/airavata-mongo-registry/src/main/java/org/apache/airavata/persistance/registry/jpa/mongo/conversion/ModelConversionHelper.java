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
package org.apache.airavata.persistance.registry.jpa.mongo.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Group;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.User;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.appcatalog.idot.InputDataObjectTypeDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.appcatalog.idot.InputDataObjectTypeSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.appcatalog.odot.OutputDataObjectTypeDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.appcatalog.odot.OutputDataObjectTypeSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.ExperimentDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.ExperimentSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.aidh.AdvancedInputDataHandlingDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.aidh.AdvancedInputDataHandlingSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.aodh.AdvancedOutputDataHandlingDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.aodh.AdvancedOutputDataHandlingSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.appstatus.ApplicationStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.appstatus.ApplicationStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.crsh.ComputationalResourceSchedulingDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.crsh.ComputationalResourceSchedulingSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.datatrdetails.DataTransferDetailsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.datatrdetails.DataTransferDetailsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.errdetails.ErrorDetailsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.errdetails.ErrorDetailsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.expstatus.ExperimentStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.expstatus.ExperimentStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.expsummary.ExperimentSummaryDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.expsummary.ExperimentSummarySerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.jobdetails.JobDetailsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.jobdetails.JobDetailsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.jobstatus.JobStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.jobstatus.JobStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.qosp.QualityOfServiceParamsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.qosp.QualityOfServiceParamsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.taskdetails.TaskDetailsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.taskdetails.TaskDetailsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.taskstatus.TaskStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.taskstatus.TaskStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.trstatus.TransferStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.trstatus.TransferStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.ucdata.UserConfigurationDataDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.ucdata.UserConfigurationDataSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.validationrslt.ValidationResultsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.validationrslt.ValidationResultsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.wfnd.WorkflowNodeDetailsDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.wfnd.WorkflowNodeDetailsSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.wfns.WorkflowNodeStatusDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.wfns.WorkflowNodeStatusSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.gateway.GatewayDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.gateway.GatewaySerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.group.GroupDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.group.GroupSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.project.ProjectDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.project.ProjectSerializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.user.UserDeserializer;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.user.UserSerializer;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is utility class for model conversion of thrift to/from json
 */
public class ModelConversionHelper {
    private final static Logger logger = LoggerFactory.getLogger(ModelConversionHelper.class);
    private ObjectMapper objectMapper;

    public ModelConversionHelper(){
        init();
    }

    /**
     * Private method to register the custom serializers and deserializers
     */
    private void init(){
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("SimpleModule",
                new Version(1,0,0,null,null,null));

        module.addSerializer(Gateway.class, new GatewaySerializer());
        module.addDeserializer(Gateway.class, new GatewayDeserializer());

        module.addSerializer(Group.class, new GroupSerializer());
        module.addDeserializer(Group.class, new GroupDeserializer());

        module.addSerializer(Project.class, new ProjectSerializer());
        module.addDeserializer(Project.class, new ProjectDeserializer());

        module.addSerializer(User.class, new UserSerializer());
        module.addDeserializer(User.class, new UserDeserializer());

        module.addSerializer(Experiment.class, new ExperimentSerializer());
        module.addDeserializer(Experiment.class, new ExperimentDeserializer());

        module.addSerializer(AdvancedInputDataHandling.class,
                new AdvancedInputDataHandlingSerializer());
        module.addDeserializer(AdvancedInputDataHandling.class,
                new AdvancedInputDataHandlingDeserializer());

        module.addSerializer(AdvancedOutputDataHandling.class,
                new AdvancedOutputDataHandlingSerializer());
        module.addDeserializer(AdvancedOutputDataHandling.class,
                new AdvancedOutputDataHandlingDeserializer());

        module.addSerializer(ApplicationStatus.class,
                new ApplicationStatusSerializer());
        module.addDeserializer(ApplicationStatus.class,
                new ApplicationStatusDeserializer());

        module.addSerializer(ComputationalResourceScheduling.class,
                new ComputationalResourceSchedulingSerializer());
        module.addDeserializer(ComputationalResourceScheduling.class,
                new ComputationalResourceSchedulingDeserializer());

        module.addSerializer(DataTransferDetails.class, new DataTransferDetailsSerializer());
        module.addDeserializer(DataTransferDetails.class, new DataTransferDetailsDeserializer());

        module.addSerializer(ErrorDetails.class, new ErrorDetailsSerializer());
        module.addDeserializer(ErrorDetails.class, new ErrorDetailsDeserializer());

        module.addSerializer(ExperimentStatus.class, new ExperimentStatusSerializer());
        module.addDeserializer(ExperimentStatus.class, new ExperimentStatusDeserializer());

        module.addSerializer(ExperimentSummary.class, new ExperimentSummarySerializer());
        module.addDeserializer(ExperimentSummary.class, new ExperimentSummaryDeserializer());

        module.addSerializer(JobDetails.class, new JobDetailsSerializer());
        module.addDeserializer(JobDetails.class, new JobDetailsDeserializer());

        module.addSerializer(JobStatus.class, new JobStatusSerializer());
        module.addDeserializer(JobStatus.class, new JobStatusDeserializer());

        module.addSerializer(QualityOfServiceParams.class,
                new QualityOfServiceParamsSerializer());
        module.addDeserializer(QualityOfServiceParams.class,
                new QualityOfServiceParamsDeserializer());

        module.addSerializer(TaskDetails.class, new TaskDetailsSerializer());
        module.addDeserializer(TaskDetails.class, new TaskDetailsDeserializer());

        module.addSerializer(TaskStatus.class, new TaskStatusSerializer());
        module.addDeserializer(TaskStatus.class, new TaskStatusDeserializer());

        module.addSerializer(TransferStatus.class, new TransferStatusSerializer());
        module.addDeserializer(TransferStatus.class, new TransferStatusDeserializer());

        module.addSerializer(UserConfigurationData.class, new UserConfigurationDataSerializer());
        module.addDeserializer(UserConfigurationData.class, new UserConfigurationDataDeserializer());

        module.addSerializer(ValidationResults.class, new ValidationResultsSerializer());
        module.addDeserializer(ValidationResults.class, new ValidationResultsDeserializer());

        module.addSerializer(WorkflowNodeDetails.class, new WorkflowNodeDetailsSerializer());
        module.addDeserializer(WorkflowNodeDetails.class, new WorkflowNodeDetailsDeserializer());

        module.addSerializer(WorkflowNodeStatus.class, new WorkflowNodeStatusSerializer());
        module.addDeserializer(WorkflowNodeStatus.class, new WorkflowNodeStatusDeserializer());

        module.addSerializer(InputDataObjectType.class, new InputDataObjectTypeSerializer());
        module.addDeserializer(InputDataObjectType.class, new InputDataObjectTypeDeserializer());

        module.addSerializer(OutputDataObjectType.class, new OutputDataObjectTypeSerializer());
        module.addDeserializer(OutputDataObjectType.class, new OutputDataObjectTypeDeserializer());

        objectMapper.registerModule(module);
    }

    /**
     * Method to serialize a thrift object to json
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public String serializeObject(TBase object) throws JsonProcessingException {
        String json = this.objectMapper.writeValueAsString(object);
        return json;
    }

    /**
     * Method to deserialize a json to the thrift object
     * @param clz
     * @param json
     * @return
     * @throws IOException
     */
    public TBase deserializeObject(Class<?> clz, String json) throws IOException {
        return (TBase)this.objectMapper.readValue(json, clz);
    }
}