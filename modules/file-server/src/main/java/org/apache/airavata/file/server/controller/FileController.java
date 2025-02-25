package org.apache.airavata.file.server.controller;

import org.apache.airavata.file.server.model.AiravataDirectory;
import org.apache.airavata.file.server.model.FileUploadResponse;
import org.apache.airavata.file.server.service.AirvataFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

@Controller
public class FileController {

    private final static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private AirvataFileService fileService;

    @GetMapping("/list/{live}/{processId}")
    @ResponseBody
    public Object listFilesRoot(@PathVariable String live,
                            @PathVariable String processId) throws Exception {
        try {
            return fileService.listFiles(processId, "");
        } catch (Exception e) {
            logger.error("Failed to list files for path {} in process {}", "root path", processId, e);
            throw e;
        }
    }
    @GetMapping("/list/{live}/{processId}/{subPath}")
    @ResponseBody
    public Object listFiles(@PathVariable String live,
                            @PathVariable String processId,
                            @PathVariable String subPath) throws Exception {
        try {
            return fileService.listFiles(processId, subPath);
        } catch (Exception e) {
            logger.error("Failed to list files for path {} in process {}", subPath, processId, e);
            throw e;
        }
    }

    @GetMapping("/download/{live}/{processId}/{subPath}")
    @ResponseBody
    public ResponseEntity downloadFile(@PathVariable String live,
                                                 @PathVariable String processId,
                                                 @PathVariable String subPath) {

        try {
            Path localPath = fileService.downloadFile(processId, subPath);
            Resource resource = new UrlResource(localPath.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + new File(subPath).getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to download file {} from process {}", subPath, processId, e);
            return ResponseEntity.internalServerError()
                    .body("An internal server error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/upload/{live}/{processId}/{subPath}")
    @ResponseBody
    public ResponseEntity uploadFile(@PathVariable String live,
                                         @PathVariable String processId,
                                         @PathVariable String subPath,
                                         @RequestParam("file") MultipartFile file) {
        try {
            String name = file.getName();
            fileService.uploadFile(processId, subPath, file);
            return ResponseEntity.ok(new FileUploadResponse(name, subPath, file.getContentType(), file.getSize()));

        } catch (Exception e) {
            logger.error("Failed to upload file {} to process {}", subPath, processId, e);
            return ResponseEntity.internalServerError()
                    .body("An internal server error occurred: " + e.getMessage());
        }
    }
}
