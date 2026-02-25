/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.application.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.airavata.research.application.model.ApplicationField;
import org.apache.airavata.research.application.model.ApplicationDeploymentDescription;
import org.apache.airavata.research.application.model.ApplicationInterfaceDescription;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.application.entity.ApplicationEntity;
import org.apache.airavata.research.application.entity.ApplicationInstallationEntity;
import org.apache.airavata.research.application.repository.ApplicationInstallationRepository;
import org.apache.airavata.research.application.repository.ApplicationRepository;
import org.apache.airavata.core.util.EnumUtil;
import org.apache.airavata.storage.resource.model.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter service that maps application entities to the legacy model types expected by
 * the workflow engine.
 *
 * <p>Handles application interface descriptions, deployment descriptions, and input/output
 * field conversion from {@link ApplicationField} to the typed {@link ApplicationInput} and
 * {@link ApplicationOutput} legacy models.
 *
 * <p>All methods are read-only and return {@code null} or empty collections when an
 * application is not found.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ApplicationAdapter.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationInstallationRepository applicationInstallationRepository;

    public ApplicationAdapter(
            ApplicationRepository applicationRepository,
            ApplicationInstallationRepository applicationInstallationRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationInstallationRepository = applicationInstallationRepository;
    }

    // -------------------------------------------------------------------------
    // Application interface
    // -------------------------------------------------------------------------

    /**
     * Load an {@link ApplicationEntity} by ID and map it to an {@link ApplicationInterfaceDescription}.
     *
     * <p>The application serves as its own module, so {@code applicationModules} is a single-element
     * list containing the application ID. Input and output fields are converted from {@link ApplicationField}
     * lists.
     *
     * @param appInterfaceId the application identifier
     * @return mapped description, or {@code null} if the application does not exist
     */
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) {
        Optional<ApplicationEntity> optional = applicationRepository.findById(appInterfaceId);
        if (optional.isEmpty()) {
            log.debug("getApplicationInterface: no application found for id={}", appInterfaceId);
            return null;
        }
        ApplicationEntity app = optional.get();

        ApplicationInterfaceDescription description = new ApplicationInterfaceDescription();
        description.setApplicationInterfaceId(app.getApplicationId());
        description.setApplicationName(app.getName());
        description.setApplicationDescription(app.getDescription());
        description.setApplicationModules(List.of(app.getApplicationId()));

        List<ApplicationInput> inputs = convertFieldsToInputs(app.getInputs());
        description.setApplicationInputs(inputs);

        List<ApplicationOutput> outputs = convertFieldsToOutputs(app.getOutputs());
        description.setApplicationOutputs(outputs);

        return description;
    }

    // -------------------------------------------------------------------------
    // Application deployments
    // -------------------------------------------------------------------------

    /**
     * Find all {@link ApplicationInstallationEntity} records for the given application module ID
     * and map each to an {@link ApplicationDeploymentDescription}.
     *
     * @param appModuleId the application identifier
     * @return list of deployment descriptions; empty list if no installations exist
     */
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) {
        List<ApplicationInstallationEntity> installations =
                applicationInstallationRepository.findByApplicationId(appModuleId);

        return installations.stream()
                .map(installation -> {
                    ApplicationDeploymentDescription deployment = new ApplicationDeploymentDescription();
                    deployment.setAppDeploymentId(installation.getInstallationId());
                    deployment.setAppModuleId(appModuleId);
                    deployment.setComputeHostId(installation.getResourceId());
                    deployment.setExecutablePath(installation.getInstallPath());
                    return deployment;
                })
                .toList();
    }

    // -------------------------------------------------------------------------
    // Application inputs / outputs
    // -------------------------------------------------------------------------

    /**
     * Load an application by ID and return its inputs as {@link ApplicationInput} list.
     *
     * @param appInterfaceId the application identifier
     * @return list of inputs; empty list if the application does not exist or has no inputs
     */
    public List<ApplicationInput> getApplicationInputs(String appInterfaceId) {
        Optional<ApplicationEntity> optional = applicationRepository.findById(appInterfaceId);
        if (optional.isEmpty()) {
            log.debug("getApplicationInputs: no application found for id={}", appInterfaceId);
            return Collections.emptyList();
        }
        return convertFieldsToInputs(optional.get().getInputs());
    }

    /**
     * Load an application by ID and return its outputs as {@link ApplicationOutput} list.
     *
     * @param appInterfaceId the application identifier
     * @return list of outputs; empty list if the application does not exist or has no outputs
     */
    public List<ApplicationOutput> getApplicationOutputs(String appInterfaceId) {
        Optional<ApplicationEntity> optional = applicationRepository.findById(appInterfaceId);
        if (optional.isEmpty()) {
            log.debug("getApplicationOutputs: no application found for id={}", appInterfaceId);
            return Collections.emptyList();
        }
        return convertFieldsToOutputs(optional.get().getOutputs());
    }

    // -------------------------------------------------------------------------
    // Application name map
    // -------------------------------------------------------------------------

    /**
     * Return a map of application IDs to names for a given gateway.
     *
     * @param gatewayId the gateway identifier
     * @return map; empty if no applications exist for the gateway
     */
    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) {
        return applicationRepository.findByGatewayId(gatewayId).stream()
                .collect(Collectors.toMap(ApplicationEntity::getApplicationId, ApplicationEntity::getName));
    }

    // -------------------------------------------------------------------------
    // Field conversion helpers
    // -------------------------------------------------------------------------

    /**
     * Convert a list of {@link ApplicationField} definitions to {@link ApplicationInput} instances.
     *
     * @param fields source fields (may be {@code null} or empty)
     * @return converted list; never {@code null}
     */
    public List<ApplicationInput> convertFieldsToInputs(List<ApplicationField> fields) {
        if (fields == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, fields.size())
                .mapToObj(i -> convertApplicationFieldToInput(fields.get(i), i))
                .toList();
    }

    /**
     * Convert a list of {@link ApplicationField} definitions to {@link ApplicationOutput} instances.
     *
     * @param fields source fields (may be {@code null} or empty)
     * @return converted list; never {@code null}
     */
    public List<ApplicationOutput> convertFieldsToOutputs(List<ApplicationField> fields) {
        if (fields == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, fields.size())
                .mapToObj(i -> convertApplicationFieldToOutput(fields.get(i), i))
                .toList();
    }

    /**
     * Convert a single {@link ApplicationField} to an {@link ApplicationInput}.
     *
     * <p>The {@code type} string on the field is resolved via {@link DataType#valueOf}; if the
     * value is unrecognised or {@code null} the type defaults to {@link DataType#STRING}.
     *
     * @param field  source field
     * @param index  zero-based position used as {@code inputOrder}
     * @return converted input object
     */
    public ApplicationInput convertApplicationFieldToInput(ApplicationField field, int index) {
        ApplicationInput input = new ApplicationInput();
        input.setName(field.getName());
        input.setValue(field.getDefaultValue());
        input.setType(resolveDataType(field.getType()));
        input.setInputOrder(index);
        input.setIsRequired(field.isRequired());
        input.setUserFriendlyDescription(field.getDescription());
        return input;
    }

    /**
     * Convert a single {@link ApplicationField} to an {@link ApplicationOutput}.
     *
     * <p>The {@code type} string on the field is resolved via {@link DataType#valueOf}; if the
     * value is unrecognised or {@code null} the type defaults to {@link DataType#STRING}.
     *
     * @param field  source field
     * @param index  zero-based position (informational; not stored on the output type)
     * @return converted output object
     */
    public ApplicationOutput convertApplicationFieldToOutput(ApplicationField field, int index) {
        ApplicationOutput output = new ApplicationOutput();
        output.setName(field.getName());
        output.setType(resolveDataType(field.getType()));
        output.setIsRequired(field.isRequired());
        return output;
    }

    /**
     * Safely resolve a {@link DataType} from the string token stored on an {@link ApplicationField}.
     *
     * @param typeToken string such as {@code "STRING"}, {@code "INTEGER"}, {@code "FILE"} etc.
     * @return matched enum constant, or {@link DataType#STRING} when the token is blank or unknown
     */
    public DataType resolveDataType(String typeToken) {
        if (typeToken == null || typeToken.isBlank()) {
            return DataType.STRING;
        }
        return EnumUtil.safeValueOf(DataType.class, typeToken.toUpperCase(), DataType.STRING);
    }
}
