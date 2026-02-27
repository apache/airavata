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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.nio.file.Path;
import org.apache.airavata.file.model.FileUploadResponse;
import org.apache.airavata.file.service.FileService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * File API Controller - Part of the unified HTTP server.
 *
 * <p>This controller provides HTTP endpoints for file operations (list, upload, download)
 * for process data directories, running as part of the unified HTTP server on port 8080
 * (configurable via {@code airavata.services.http.server.port}).
 *
 * <p><b>External API:</b> This is part of one of three external API layers in Airavata:
 * <ul>
 *   <li>HTTP Server (port 8080):
 *       <ul>
 *         <li>Airavata API - HTTP Endpoints for Airavata API functions</li>
 *         <li>File API (this controller) - HTTP Endpoints for file upload/download</li>
 *         <li>Agent API - HTTP Endpoints for interactive job contexts</li>
 *         <li>Research API - HTTP Endpoints for use by research hub</li>
 *       </ul>
 *   </li>
 *   <li>gRPC Server (port 9090) - For airavata binaries to open persistent channels with airavata APIs</li>
 *   <li>Dapr gRPC (port 50001) - Sidecar for pub/sub, state, and workflow execution</li>
 * </ul>
 *
 * <p><b>Endpoints:</b> (all under base path {@code /api/v1/files})
 * <ul>
 *   <li>{@code GET /api/v1/files/list/{live}/{processId}} - List files in process root directory</li>
 *   <li>{@code GET /api/v1/files/list/{live}/{processId}/{*subPath}} - List files in subdirectory or get file info</li>
 *   <li>{@code GET /api/v1/files/download/{live}/{processId}/{*subPath}} - Download a file</li>
 *   <li>{@code POST /api/v1/files/upload/{live}/{processId}/{*subPath}} - Upload a file</li>
 * </ul>
 *
 * <p><b>Path Parameters:</b>
 * <ul>
 *   <li>{@code live} - Indicates whether accessing live or archived process data</li>
 *   <li>{@code processId} - Process identifier</li>
 *   <li>{@code subPath} - Relative path within the process data directory</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code airavata.services.fileserver.enabled} - Enable/disable File API (default: true)</li>
 *   <li>{@code airavata.services.http.server.port} - Unified HTTP server port (default: 8080)</li>
 *   <li>{@code airavata.services.fileserver.spring.servlet.multipart.max-file-size} - Max upload size (default: 10MB)</li>
 *   <li>{@code airavata.services.fileserver.spring.servlet.multipart.max-request-size} - Max request size (default: 10MB)</li>
 * </ul>
 *
 * @see org.apache.airavata.file.service.FileService
 */
@Controller
@RequestMapping("/api/v1/files")
@Tag(name = "Files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list/{live}/{processId}")
    @ResponseBody
    public Object listFilesRoot(@PathVariable String live, @PathVariable String processId) throws Exception {
        return fileService.listDir(processId, "/");
    }

    @GetMapping("/list/{live}/{processId}/{*subPath}")
    @ResponseBody
    public Object listFiles(@PathVariable String live, @PathVariable String processId, @PathVariable String subPath)
            throws Exception {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        var info = fileService.getInfo(processId, relPath);
        if (info.isDirectory()) {
            return fileService.listDir(processId, relPath);
        } else {
            return fileService.listFile(processId, relPath);
        }
    }

    @GetMapping("/download/{live}/{processId}/{*subPath}")
    @ResponseBody
    public ResponseEntity<?> downloadFile(
            @PathVariable String live, @PathVariable String processId, @PathVariable String subPath) throws Exception {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        var fileName = new File(relPath).getName();
        Path localPath = fileService.downloadFile(processId, relPath);
        var resource = new UrlResource(localPath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
                .body(resource);
    }

    @PostMapping("/upload/{live}/{processId}/{*subPath}")
    @ResponseBody
    public ResponseEntity<?> uploadFile(
            @PathVariable String live,
            @PathVariable String processId,
            @PathVariable String subPath,
            @RequestParam("file") MultipartFile file)
            throws Exception {
        var relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        var name = file.getName();
        fileService.uploadFile(processId, relPath, file);
        return ResponseEntity.ok(new FileUploadResponse(name, relPath, file.getContentType(), file.getSize()));
    }
}
