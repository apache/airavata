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

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.MediaTypeNames;
import com.linecorp.armeria.common.multipart.MultipartFile;
import com.linecorp.armeria.server.annotation.Consumes;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.airavata.api.file.FileUploadResponse;
import org.apache.airavata.orchestration.service.AirvataFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private AirvataFileService fileService;

    @Get("/list/{live}/{processId}")
    @ProducesJson
    public Object listFilesRoot(@Param String live, @Param String processId) throws Exception {
        String relPath = "/";
        try {
            return fileService.listDir(processId, relPath);
        } catch (Exception e) {
            logger.error("Failed to list files for path {} in process {}", "root path", processId, e);
            throw e;
        }
    }

    @Get("regex:^/list/(?<live>[^/]+)/(?<processId>[^/]+)/(?<subPath>.+)$")
    @ProducesJson
    public Object listFiles(@Param String live, @Param String processId, @Param String subPath) throws Exception {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        try {
            var info = fileService.getInfo(processId, relPath);
            if (info.isDirectory()) {
                return fileService.listDir(processId, relPath);
            } else {
                return fileService.listFile(processId, relPath);
            }
        } catch (Exception e) {
            logger.error("Failed to list files for path {} in process {}", relPath, processId, e);
            throw e;
        }
    }

    @Get("regex:^/download/(?<live>[^/]+)/(?<processId>[^/]+)/(?<subPath>.+)$")
    public HttpResponse downloadFile(@Param String live, @Param String processId, @Param String subPath) {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        String fileName = new File(relPath).getName();
        try {
            Path localPath = fileService.downloadFile(processId, relPath);
            byte[] fileBytes = Files.readAllBytes(localPath);
            return HttpResponse.builder()
                    .ok()
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName))
                    .content(MediaType.OCTET_STREAM, fileBytes)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to download file {} from process {}", relPath, processId, e);
            return HttpResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MediaType.PLAIN_TEXT_UTF_8,
                    "An internal server error occurred: " + e.getMessage());
        }
    }

    @Post("regex:^/upload/(?<live>[^/]+)/(?<processId>[^/]+)/(?<subPath>.+)$")
    @Consumes(MediaTypeNames.MULTIPART_FORM_DATA)
    @ProducesJson
    public HttpResponse uploadFile(
            @Param String live, @Param String processId, @Param String subPath, @Param MultipartFile file) {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        try {
            String name = file.filename();
            long size = Files.size(file.path());
            fileService.uploadFile(processId, relPath, Files.newInputStream(file.path()), name, size);
            String contentType = file.headers().contentType() != null
                    ? file.headers().contentType().toString()
                    : "";
            return HttpResponse.ofJson(FileUploadResponse.newBuilder()
                    .setName(name)
                    .setUri(relPath)
                    .setType(contentType)
                    .setSize(size)
                    .build());
        } catch (Exception e) {
            logger.error("Failed to upload file {} to process {}", relPath, processId, e);
            return HttpResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MediaType.PLAIN_TEXT_UTF_8,
                    "An internal server error occurred: " + e.getMessage());
        }
    }
}
