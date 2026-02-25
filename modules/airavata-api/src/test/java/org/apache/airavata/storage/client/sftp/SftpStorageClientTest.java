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
package org.apache.airavata.storage.client.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.StorageResourceAdapter;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.storage.resource.model.DataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link SftpStorageClient}.
 *
 * <p>Verifies input staging (stageIn) and output staging (stageOut) behavior
 * including handling of null inputs, type filtering, optional/required fields,
 * URI parsing, URI collections, and experiment output persistence.
 */
@ExtendWith(MockitoExtension.class)
class SftpStorageClientTest {

    @Mock
    private DataStagingSupport dataStagingSupport;

    @Mock
    private SftpClient sftpClient;

    @Mock
    private AdapterSupport adapterSupport;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ServerProperties serverProperties;

    @Mock
    private TaskContext context;

    @Mock
    private ProcessModel processModel;

    @Mock
    private AgentAdapter computeAdapter;

    @Mock
    private StorageResourceAdapter storageAdapter;

    private SftpStorageClient storageClient;

    @BeforeEach
    void setUp() throws Exception {
        storageClient = new SftpStorageClient(
                dataStagingSupport, sftpClient, adapterSupport, experimentRepository, serverProperties);

        // Common stubs used by most tests — lenient so unused stubs don't fail
        lenient().when(context.getProcessId()).thenReturn("process-1");
        lenient().when(context.getExperimentId()).thenReturn("exp-1");
        lenient().when(context.getTaskId()).thenReturn("task-1");
        lenient().when(context.getProcessModel()).thenReturn(processModel);
        lenient().when(context.getWorkingDir()).thenReturn("/scratch/process1");
    }

    // -------------------------------------------------------------------------
    // stageIn tests
    // -------------------------------------------------------------------------

    @Test
    void stageIn_withNoInputs_returnsSuccess() {
        when(processModel.getProcessInputs()).thenReturn(null);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageIn with null inputs must return Success");
        verifyNoInteractions(sftpClient);
        verifyNoInteractions(dataStagingSupport);
    }

    @Test
    void stageIn_skipsNonUriInputs() throws Exception {
        ApplicationInput stringInput = new ApplicationInput();
        stringInput.setName("greeting");
        stringInput.setValue("hello");
        stringInput.setType(DataType.STRING);
        stringInput.setIsRequired(true);

        when(processModel.getProcessInputs()).thenReturn(List.of(stringInput));
        when(processModel.getInputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageIn with only STRING inputs must return Success");
        verify(dataStagingSupport, never()).transferFileToComputeResource(
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void stageIn_skipsOptionalNullInputs() throws Exception {
        ApplicationInput optionalInput = new ApplicationInput();
        optionalInput.setName("optional-file");
        optionalInput.setValue(null);
        optionalInput.setType(DataType.URI);
        optionalInput.setIsRequired(false);

        when(processModel.getProcessInputs()).thenReturn(List.of(optionalInput));
        when(processModel.getInputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageIn with optional null URI input must return Success");
        verify(dataStagingSupport, never()).transferFileToComputeResource(
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void stageIn_failsOnRequiredNullInput() throws Exception {
        ApplicationInput requiredInput = new ApplicationInput();
        requiredInput.setName("required-file");
        requiredInput.setValue(null);
        requiredInput.setType(DataType.URI);
        requiredInput.setIsRequired(true);

        when(processModel.getProcessInputs()).thenReturn(List.of(requiredInput));
        when(processModel.getInputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Failure.class, result,
                "stageIn with required null URI input must return Failure");
        DagTaskResult.Failure failure = (DagTaskResult.Failure) result;
        assertTrue(failure.fatal(), "Required null input failure must be fatal");
        assertTrue(failure.reason().contains("required-file"),
                "Failure reason must mention the input name");
    }

    @Test
    void stageIn_transfersUriInput() throws Exception {
        ApplicationInput uriInput = new ApplicationInput();
        uriInput.setName("input-file");
        uriInput.setValue("scp://host/path/to/file.txt");
        uriInput.setType(DataType.URI);
        uriInput.setIsRequired(true);

        when(processModel.getProcessInputs()).thenReturn(List.of(uriInput));
        when(processModel.getInputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageIn with valid URI input must return Success");
        // URI path is "/path/to/file.txt", destination is workingDir + "/" + "file.txt"
        verify(dataStagingSupport).transferFileToComputeResource(
                eq("/path/to/file.txt"),
                eq("/scratch/process1/file.txt"),
                eq(computeAdapter),
                eq(storageAdapter),
                eq("process-1"));
    }

    @Test
    void stageIn_handlesUriCollection() throws Exception {
        ApplicationInput collectionInput = new ApplicationInput();
        collectionInput.setName("input-files");
        collectionInput.setValue("scp://host/data/file1.txt,scp://host/data/file2.txt");
        collectionInput.setType(DataType.URI_COLLECTION);
        collectionInput.setIsRequired(true);

        when(processModel.getProcessInputs()).thenReturn(List.of(collectionInput));
        when(processModel.getInputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        DagTaskResult result = storageClient.stageIn(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageIn with URI_COLLECTION input must return Success");
        verify(dataStagingSupport).transferFileToComputeResource(
                eq("/data/file1.txt"),
                eq("/scratch/process1/file1.txt"),
                eq(computeAdapter),
                eq(storageAdapter),
                eq("process-1"));
        verify(dataStagingSupport).transferFileToComputeResource(
                eq("/data/file2.txt"),
                eq("/scratch/process1/file2.txt"),
                eq(computeAdapter),
                eq(storageAdapter),
                eq("process-1"));
        verify(dataStagingSupport, times(2)).transferFileToComputeResource(
                anyString(), anyString(), any(), any(), anyString());
    }

    // -------------------------------------------------------------------------
    // stageOut tests
    // -------------------------------------------------------------------------

    @Test
    void stageOut_withNoOutputs_returnsSuccess() {
        when(processModel.getProcessOutputs()).thenReturn(null);

        DagTaskResult result = storageClient.stageOut(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageOut with null outputs must return Success");
        verifyNoInteractions(sftpClient);
        verifyNoInteractions(dataStagingSupport);
    }

    @Test
    void stageOut_transfersUriOutput_andSavesToExperiment() throws Exception {
        ApplicationOutput uriOutput = new ApplicationOutput();
        uriOutput.setName("result-file");
        uriOutput.setValue("output.txt");
        uriOutput.setType(DataType.URI);

        when(processModel.getProcessOutputs()).thenReturn(List.of(uriOutput));
        when(processModel.getOutputStorageResourceId()).thenReturn("storage-1");
        when(sftpClient.resolveStorageAdapter(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(storageAdapter);
        when(sftpClient.getComputeResourceAdapter(any(), any(), anyString()))
                .thenReturn(computeAdapter);

        // Stub dataStagingSupport for output transfer
        when(dataStagingSupport.buildDestinationFilePath(
                eq("/scratch/process1"), eq("output.txt"), eq(context)))
                .thenReturn("/scratch/process1/exp-data/output.txt");
        when(dataStagingSupport.transferFileToStorage(
                eq("/scratch/process1/output.txt"),
                eq("/scratch/process1/exp-data/output.txt"),
                eq("output.txt"),
                eq(computeAdapter),
                eq(storageAdapter),
                eq("process-1")))
                .thenReturn(true);
        when(dataStagingSupport.escapeSpecialCharacters("file:///scratch/process1/exp-data/output.txt"))
                .thenReturn("file:///scratch/process1/exp-data/output.txt");

        // Stub experimentRepository for saveExperimentOutput
        ExperimentEntity experimentEntity = new ExperimentEntity();
        experimentEntity.setExperimentId("exp-1");
        experimentEntity.setOutputs(new ArrayList<>());
        when(experimentRepository.findById("exp-1")).thenReturn(Optional.of(experimentEntity));

        DagTaskResult result = storageClient.stageOut(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "stageOut with valid URI output must return Success");

        // Verify the file was transferred
        verify(dataStagingSupport).transferFileToStorage(
                eq("/scratch/process1/output.txt"),
                eq("/scratch/process1/exp-data/output.txt"),
                eq("output.txt"),
                eq(computeAdapter),
                eq(storageAdapter),
                eq("process-1"));

        // Verify the experiment output was saved
        verify(experimentRepository).save(experimentEntity);
        assertEquals(1, experimentEntity.getOutputs().size(),
                "Exactly one output should be persisted on the experiment entity");
        ExperimentOutputEntity savedOutput = experimentEntity.getOutputs().get(0);
        assertEquals("result-file", savedOutput.getName(),
                "Saved output name must match the application output name");
        assertEquals("file:///scratch/process1/exp-data/output.txt", savedOutput.getValue(),
                "Saved output value must be the escaped file URI of the destination path");
    }
}
