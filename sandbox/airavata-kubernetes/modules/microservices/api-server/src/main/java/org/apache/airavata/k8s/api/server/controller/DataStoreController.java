package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.resources.data.DataEntryResource;
import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.service.data.DataStoreService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@RestController
@RequestMapping(path="/data")
public class DataStoreController {

    @Resource
    private DataStoreService dataStoreService;

    @PostMapping("{taskId}/{expOutputId}/upload")
    public long uploadData(@RequestParam("file") MultipartFile file, @PathVariable("taskId") long taskId,
                           @PathVariable("expOutputId") long expOutputId, RedirectAttributes redirectAttributes) {

        System.out.println("Received data for task id " + taskId + " and experiment output id " + expOutputId);
        if (file.isEmpty()) {
            throw new ServerRuntimeException("Data file is empty");
        }
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            return this.dataStoreService.createEntry(taskId, expOutputId, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerRuntimeException("Failed to store file", e);
        }
    }

    @GetMapping("process/{id}")
    public List<DataEntryResource> getAllEntriesForProcess(@PathVariable("id") long processId) {
        return this.dataStoreService.getEntriesForProcess(processId);
    }
}
