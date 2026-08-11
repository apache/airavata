package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.data.dto.SCPDataRequestDto;
import org.apache.airavata.data.dto.SCPDataResponseDto;
import org.apache.airavata.data.service.SCPDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The owning user is never a request parameter — see {@code SCPDataService.createData}. */
@RestController
@RequestMapping("/api/v1/scp-data")
public class SCPDataController {

    private final SCPDataService scpDataService;

    public SCPDataController(SCPDataService scpDataService) {
        this.scpDataService = scpDataService;
    }

    /** Every data registration across every user — admin only. */
    @GetMapping
    public List<SCPDataResponseDto> getAllData() {
        return scpDataService.getAllData();
    }

    /** The caller's own data registrations. */
    @GetMapping("/me")
    public List<SCPDataResponseDto> getMyData() {
        return scpDataService.getMyData();
    }

    @GetMapping("/{id}")
    public SCPDataResponseDto getData(@PathVariable String id) {
        return scpDataService.getData(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SCPDataResponseDto createData(@Valid @RequestBody SCPDataRequestDto request) {
        return scpDataService.createData(request);
    }

    @PutMapping("/{id}")
    public SCPDataResponseDto updateData(@PathVariable String id, @Valid @RequestBody SCPDataRequestDto request) {
        return scpDataService.updateData(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteData(@PathVariable String id) {
        scpDataService.deleteData(id);
    }
}
