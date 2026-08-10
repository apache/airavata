package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.process.dto.BatchJobProcessRequestDto;
import org.apache.airavata.process.dto.BatchJobProcessResponseDto;
import org.apache.airavata.process.service.BatchJobProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The owning user is never a request parameter — see {@code BatchJobProcessService.createProcess}. */
@RestController
@RequestMapping("/api/v1/batch-job-processes")
public class BatchJobProcessController {

    private final BatchJobProcessService batchJobProcessService;

    public BatchJobProcessController(BatchJobProcessService batchJobProcessService) {
        this.batchJobProcessService = batchJobProcessService;
    }

    /** Lists every process, or only those of {@code deploymentId} when supplied. */
    @GetMapping
    public List<BatchJobProcessResponseDto> getAllProcesses(@RequestParam(required = false) String deploymentId) {
        return deploymentId == null
                ? batchJobProcessService.getAllProcesses()
                : batchJobProcessService.getProcessesByDeployment(deploymentId);
    }

    @GetMapping("/{processId}")
    public BatchJobProcessResponseDto getProcess(@PathVariable String processId) {
        return batchJobProcessService.getProcess(processId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchJobProcessResponseDto createProcess(@Valid @RequestBody BatchJobProcessRequestDto request) {
        return batchJobProcessService.createProcess(request);
    }

    @PutMapping("/{processId}")
    public BatchJobProcessResponseDto updateProcess(
            @PathVariable String processId, @Valid @RequestBody BatchJobProcessRequestDto request) {
        return batchJobProcessService.updateProcess(processId, request);
    }

    @DeleteMapping("/{processId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProcess(@PathVariable String processId) {
        batchJobProcessService.deleteProcess(processId);
    }
}
