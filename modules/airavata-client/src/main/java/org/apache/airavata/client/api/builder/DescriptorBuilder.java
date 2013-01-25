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

package org.apache.airavata.client.api.builder;

import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;
import org.apache.xmlbeans.SchemaType;

import java.util.List;

/**
 * This class provides set of utility methods to create descriptors. Following descriptors are included.
 * <ol>
 * <li>HostDescription - org.apache.airavata.commons.gfac.type.HostDescription</li>
 * </ol>
 */
public class DescriptorBuilder {

    private static final String IP_ADDRESS_VALIDATION_STRING = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final String HOST_NAME_VALIDATION_STRING = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    /**
     * Builds a host descriptor object. Host descriptor gives information about application hosted machine.
     * 
     * @param type
     *            The host type. Following types are available at the moment.
     *            <ol>
     *            <li>org.apache.airavata.schemas.gfac.GlobusHostType</li>
     *            <li>org.apache.airavata.schemas.gfac.Ec2HostType</li>
     *            <li>org.apache.airavata.schemas.gfac.GsisshHostType</li>
     *            <li>org.apache.airavata.schemas.gfac.UnicoreHostType</li>
     *            </ol>
     * @param hostName
     *            An unique id given to hosted machine. This could be any name. But should be unique across the
     *            workflow.
     * @param hostAddress
     *            This is the IP address where application is running. This should be a valid IP address or valid host
     *            name.
     * @return The org.apache.airavata.commons.gfac.type.HostDescription object.
     * @throws AiravataAPIInvocationException
     *             If provided host address does not comply with IP address format or host name format.
     */
    public HostDescription buildHostDescription(HostDescriptionType type, String hostName, String hostAddress)
            throws AiravataAPIInvocationException {

        return buildHostDescription(type.schemaType(), hostName, hostAddress);

    }

    /**
     * Builds a host descriptor object. Host descriptor gives information about application hosted machine.
     * 
     * @param schemaType
     *            The host type as per the schema. All available schema types are listed in
     *            org.apache.xmlbeans.SchemaType
     * @param hostName
     *            An unique id given to hosted machine. This could be any name. But should be unique across the
     *            workflow.
     * @param hostAddress
     *            This is the IP address where application is running. This should be a valid IP address or valid host
     *            name.
     * @return The org.apache.airavata.commons.gfac.type.HostDescription object.
     * @throws AiravataAPIInvocationException
     *             If provided host address does not comply with IP address format or host name format.
     */
    public HostDescription buildHostDescription(SchemaType schemaType, String hostName, String hostAddress)
            throws AiravataAPIInvocationException {

        if (!validateHostAddress(hostAddress)) {
            throw new AiravataAPIInvocationException("Invalid host address. Host address should be "
                    + "either an IP address or a valid host name.");
        }

        HostDescription hostDescription = new HostDescription(schemaType);
        hostDescription.getType().setHostName(hostName);
        hostDescription.getType().setHostAddress(hostAddress);

        return hostDescription;

    }

    /**
     * Create an input parameter.
     * 
     * @param parameterName
     *            The input parameter name.
     * @param parameterDescription
     *            A short description about the input parameter.
     * @param parameterDataType
     *            Input parameter type. E.g :- String, Integer etc .. All possible types are define in DataType.Enum
     *            class. E.g :- DataType.String
     * @see org.apache.airavata.schemas.gfac.DataType.Enum
     * @return Encapsulating InputParameterType object.
     */
    public InputParameterType buildInputParameterType(String parameterName, String parameterDescription,
            DataType.Enum parameterDataType) {

        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName(parameterName);
        parameter.setParameterDescription(parameterDescription);

        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(parameterDataType);
        parameterType.setName(parameterDataType.toString());

        return parameter;

    }

    /**
     * Create an input parameter.
     * 
     * @param parameterName
     *            The input parameter name.
     * @param parameterDescription
     *            A short description about the input parameter.
     * @param parameterDataType
     *            Input parameter type. E.g :- String, Integer etc .. All possible types are define in DataType.Enum
     *            class. E.g :- DataType.String
     * @see org.apache.airavata.schemas.gfac.DataType.Enum
     * @return Encapsulating InputParameterType object.
     */
    public OutputParameterType buildOutputParameterType(String parameterName, String parameterDescription,
            DataType.Enum parameterDataType) {

        OutputParameterType parameter = OutputParameterType.Factory.newInstance();
        parameter.setParameterName(parameterName);
        parameter.setParameterDescription(parameterDescription);

        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(parameterDataType);
        parameterType.setName(parameterDataType.toString());

        return parameter;

    }

    /**
     * Creates a ServiceDescription object. This includes information about the service. Mainly we are focusing on
     * following details about the service.
     * <ol>
     * <li>A name for service</li>
     * <li>A short description about the service</li>
     * <li>A what are input data types</li>
     * <li>A what are output data types</li>
     * </ol>
     * 
     * @param serviceName
     *            Name of the service.
     * @param description
     *            A short description about the service.
     * @param inputParameterTypes
     *            Input parameter types.
     * @param outputParameterTypes
     *            Output parameter types.
     * @return A ServiceDescription object with above information encapsulated.
     */
    public ServiceDescription buildServiceDescription(String serviceName, String description,
            List<InputParameterType> inputParameterTypes, List<OutputParameterType> outputParameterTypes) {

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.getType().setName(serviceName);
        serviceDescription.getType().setDescription(description);

        serviceDescription.getType().setInputParametersArray(
                inputParameterTypes.toArray(new InputParameterType[inputParameterTypes.size()]));
        serviceDescription.getType().setOutputParametersArray(
                outputParameterTypes.toArray(new OutputParameterType[outputParameterTypes.size()]));

        return serviceDescription;

    }

    public ApplicationDescription buildApplicationDeploymentDescription(String applicationName, String executablePath,
            String workingDirectory) {

        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription
                .getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue(applicationName);
        applicationDeploymentDescriptionType.setExecutableLocation(executablePath);
        applicationDeploymentDescriptionType.setScratchWorkingDirectory(workingDirectory);

        return applicationDeploymentDescription;

    }

    private boolean validateHostAddress(String hostAddress) {
        return hostAddress.matches(IP_ADDRESS_VALIDATION_STRING) || hostAddress.matches(HOST_NAME_VALIDATION_STRING);
    }
}
