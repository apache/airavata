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
package org.apache.airavata.server.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.airavata.orchestration.service.AirvataFileService;
import org.apache.airavata.storage.model.AiravataDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private static final AirvataFileService fileService = Mockito.mock(AirvataFileService.class);

    @RegisterExtension
    static ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
            var controller = new FileController();
            // Inject mock via reflection since @Autowired is field-level
            try {
                var field = FileController.class.getDeclaredField("fileService");
                field.setAccessible(true);
                field.set(controller, fileService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sb.annotatedService("/api/v1/files", controller);
        }
    };

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listFilesRoot_returnsJson() throws Exception {
        var dir = new AiravataDirectory("root", 0, System.currentTimeMillis());
        when(fileService.listDir(eq("proc-1"), eq("/"))).thenReturn(dir);

        WebClient client = WebClient.of(server.httpUri());
        AggregatedHttpResponse response =
                client.get("/api/v1/files/list/true/proc-1").aggregate().join();

        assertEquals(HttpStatus.OK, response.status());
        assertTrue(response.contentType().is(com.linecorp.armeria.common.MediaType.JSON));

        JsonNode json = mapper.readTree(response.contentUtf8());
        assertEquals("root", json.get("directoryName").asText());
    }

    @Test
    void downloadFile_returnsFileBytes() throws Exception {
        byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
        Path tempFile = Files.createTempFile("test-download-", ".txt");
        Files.write(tempFile, content);

        when(fileService.downloadFile(eq("proc-2"), eq("/data/output.txt"))).thenReturn(tempFile);

        WebClient client = WebClient.of(server.httpUri());
        AggregatedHttpResponse response = client.get("/api/v1/files/download/true/proc-2/data/output.txt")
                .aggregate()
                .join();

        assertEquals(HttpStatus.OK, response.status());
        assertEquals("hello world", response.content().toStringUtf8());
        assertTrue(response.headers().get("Content-Disposition").contains("output.txt"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void downloadFile_returnsErrorOnFailure() throws Exception {
        when(fileService.downloadFile(anyString(), anyString())).thenThrow(new RuntimeException("not found"));

        WebClient client = WebClient.of(server.httpUri());
        AggregatedHttpResponse response = client.get("/api/v1/files/download/true/proc-3/missing/file.txt")
                .aggregate()
                .join();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status());
        assertTrue(response.contentUtf8().contains("not found"));
    }
}
