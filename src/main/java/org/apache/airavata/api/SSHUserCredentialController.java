package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.credentials.dto.SSHUserCredentialRequestDto;
import org.apache.airavata.credentials.dto.SSHUserCredentialResponseDto;
import org.apache.airavata.credentials.service.SSHUserCredentialService;
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

@RestController
@RequestMapping("/api/v1/ssh-credentials")
public class SSHUserCredentialController {

    private final SSHUserCredentialService sshUserCredentialService;

    public SSHUserCredentialController(SSHUserCredentialService sshUserCredentialService) {
        this.sshUserCredentialService = sshUserCredentialService;
    }

    /** Lists every credential, or only those using {@code sshKeyId} when supplied. */
    @GetMapping
    public List<SSHUserCredentialResponseDto> getAllCredentials(@RequestParam(required = false) String sshKeyId) {
        return sshKeyId == null
                ? sshUserCredentialService.getAllCredentials()
                : sshUserCredentialService.getCredentialsByKey(sshKeyId);
    }

    @GetMapping("/{sshCredentialId}")
    public SSHUserCredentialResponseDto getCredential(@PathVariable String sshCredentialId) {
        return sshUserCredentialService.getCredential(sshCredentialId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SSHUserCredentialResponseDto createCredential(
            @Valid @RequestBody SSHUserCredentialRequestDto request) {
        return sshUserCredentialService.createCredential(request);
    }

    @PutMapping("/{sshCredentialId}")
    public SSHUserCredentialResponseDto updateCredential(
            @PathVariable String sshCredentialId, @Valid @RequestBody SSHUserCredentialRequestDto request) {
        return sshUserCredentialService.updateCredential(sshCredentialId, request);
    }

    @DeleteMapping("/{sshCredentialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCredential(@PathVariable String sshCredentialId) {
        sshUserCredentialService.deleteCredential(sshCredentialId);
    }
}
