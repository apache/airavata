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
package org.apache.airavata.orchestration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import org.apache.airavata.compute.util.AgentAdaptor;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.orchestration.workflow.ProcessDataManager;
import org.apache.airavata.storage.model.AiravataDirectory;
import org.apache.airavata.storage.model.AiravataFile;
import org.apache.airavata.task.AdaptorSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AirvataFileServiceTest {

    @Mock
    AdaptorSupport adaptorSupport;

    @Mock
    RegistryServerHandler registryClient;

    @Mock
    AgentAdaptor agentAdaptor;

    @InjectMocks
    AirvataFileService fileService;

    @Test
    void getInfo_returnsFileMetadata() throws Exception {
        FileMetadata metadata = new FileMetadata();
        metadata.setName("test.txt");
        metadata.setSize(1024);

        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.getFileMetadata("/home/user/subdir/test.txt")).thenReturn(metadata);

            FileMetadata result = fileService.getInfo("proc-1", "subdir/test.txt");

            assertEquals("test.txt", result.getName());
            assertEquals(1024, result.getSize());
        }
    }

    @Test
    void listDir_returnsDirectoryWithContents() throws Exception {
        FileMetadata dirMeta = new FileMetadata();
        dirMeta.setName("workdir");
        dirMeta.setSize(4096);
        dirMeta.setDirectory(true);

        FileMetadata fileMeta = new FileMetadata();
        fileMeta.setName("output.txt");
        fileMeta.setSize(512);
        fileMeta.setDirectory(false);

        FileMetadata subDirMeta = new FileMetadata();
        subDirMeta.setName("logs");
        subDirMeta.setSize(4096);
        subDirMeta.setDirectory(true);

        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.getFileMetadata("/home/user/workdir")).thenReturn(dirMeta);
            when(agentAdaptor.listDirectory("/home/user/workdir")).thenReturn(List.of("output.txt", "logs"));
            when(agentAdaptor.getFileMetadata("/home/user/workdir/output.txt")).thenReturn(fileMeta);
            when(agentAdaptor.getFileMetadata("/home/user/workdir/logs")).thenReturn(subDirMeta);

            AiravataDirectory result = fileService.listDir("proc-1", "workdir");

            assertEquals("workdir", result.getDirectoryName());
            assertEquals(1, result.getInnerFiles().size());
            assertEquals("output.txt", result.getInnerFiles().get(0).getFileName());
            assertEquals(1, result.getInnerDirectories().size());
            assertEquals("logs", result.getInnerDirectories().get(0).getDirectoryName());
        }
    }

    @Test
    void listDir_throwsWhenPathIsNotDirectory() throws Exception {
        FileMetadata fileMeta = new FileMetadata();
        fileMeta.setName("file.txt");
        fileMeta.setDirectory(false);

        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.getFileMetadata("/home/user/file.txt")).thenReturn(fileMeta);

            assertThrows(Exception.class, () -> fileService.listDir("proc-1", "file.txt"));
        }
    }

    @Test
    void listFile_returnsFileInfo() throws Exception {
        FileMetadata fileMeta = new FileMetadata();
        fileMeta.setName("data.csv");
        fileMeta.setSize(2048);
        fileMeta.setDirectory(false);

        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.getFileMetadata("/home/user/data.csv")).thenReturn(fileMeta);

            AiravataFile result = fileService.listFile("proc-1", "data.csv");

            assertEquals("data.csv", result.getFileName());
            assertEquals(2048, result.getFileSize());
        }
    }

    @Test
    void uploadFile_delegatesToAdaptor() throws Exception {
        byte[] content = "hello world".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            fileService.uploadFile("proc-1", "output/test.txt", inputStream, "test.txt", content.length);

            verify(agentAdaptor).createDirectory("/home/user/output", true);
            verify(agentAdaptor)
                    .uploadFile(any(InputStream.class), any(FileMetadata.class), eq("/home/user/output/test.txt"));
        }
    }

    @Test
    void downloadFile_returnsPathWhenFileExists() throws Exception {
        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.doesFileExist("/home/user/result.txt")).thenReturn(true);
            doAnswer(invocation -> {
                        // Simulate download by creating the temp file content
                        String localPath = invocation.getArgument(1);
                        java.nio.file.Files.writeString(Path.of(localPath), "file content");
                        return null;
                    })
                    .when(agentAdaptor)
                    .downloadFile(eq("/home/user/result.txt"), anyString());

            Path result = fileService.downloadFile("proc-1", "result.txt");

            assertNotNull(result);
            assertTrue(result.toFile().exists());
            // cleanup
            result.toFile().delete();
        }
    }

    @Test
    void downloadFile_throwsWhenFileDoesNotExist() throws Exception {
        try (MockedConstruction<ProcessDataManager> mocked = mockConstruction(ProcessDataManager.class, (mock, ctx) -> {
            when(mock.getAgentAdaptor()).thenReturn(agentAdaptor);
            when(mock.getBaseDir()).thenReturn("/home/user/");
        })) {
            when(agentAdaptor.doesFileExist("/home/user/missing.txt")).thenReturn(false);

            assertThrows(Exception.class, () -> fileService.downloadFile("proc-1", "missing.txt"));
        }
    }
}
