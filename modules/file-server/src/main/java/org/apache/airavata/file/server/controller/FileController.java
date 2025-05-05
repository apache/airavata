package org.apache.airavata.file.server.controller;

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
    public Object listFilesRoot(@PathVariable String live, @PathVariable String processId) throws Exception {
        String relPath = "/";
        try {
            return fileService.listDir(processId, relPath);
        } catch (Exception e) {
            logger.error("Failed to list files for path {} in process {}", "root path", processId, e);
            throw e;
        }
    }
    
    @GetMapping("/list/{live}/{processId}/{*subPath}")
    @ResponseBody
    public Object listFiles(@PathVariable String live,@PathVariable String processId, @PathVariable String subPath) throws Exception {
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

    @GetMapping("/download/{live}/{processId}/{*subPath}")
    @ResponseBody
    public ResponseEntity downloadFile(@PathVariable String live, @PathVariable String processId, @PathVariable String subPath) {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        try {
            Path localPath = fileService.downloadFile(processId, relPath);
            Resource resource = new UrlResource(localPath.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + new File(relPath).getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to download file {} from process {}", relPath, processId, e);
            return ResponseEntity.internalServerError()
                    .body("An internal server error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/upload/{live}/{processId}/{*subPath}")
    @ResponseBody
    public ResponseEntity uploadFile(@PathVariable String live, @PathVariable String processId, @PathVariable String subPath, @RequestParam("file") MultipartFile file) {
        String relPath = subPath.startsWith("/") ? subPath : "/" + subPath;
        try {
            String name = file.getName();
            fileService.uploadFile(processId, relPath, file);
            return ResponseEntity.ok(new FileUploadResponse(name, relPath, file.getContentType(), file.getSize()));

        } catch (Exception e) {
            logger.error("Failed to upload file {} to process {}", relPath, processId, e);
            return ResponseEntity.internalServerError()
                    .body("An internal server error occurred: " + e.getMessage());
        }
    }
}
