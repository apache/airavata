package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.credentials.dto.SSHKeyRequestDto;
import org.apache.airavata.credentials.dto.SSHKeyResponseDto;
import org.apache.airavata.credentials.service.SSHKeyService;
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

/** Responses expose only the key id, name and public key — never the private key. */
@RestController
@RequestMapping("/api/v1/ssh-keys")
public class SSHKeyController {

    private final SSHKeyService sshKeyService;

    public SSHKeyController(SSHKeyService sshKeyService) {
        this.sshKeyService = sshKeyService;
    }

    @GetMapping
    public List<SSHKeyResponseDto> getAllKeys() {
        return sshKeyService.getAllKeys();
    }

    @GetMapping("/{sshKeyId}")
    public SSHKeyResponseDto getKey(@PathVariable String sshKeyId) {
        return sshKeyService.getKey(sshKeyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SSHKeyResponseDto createKey(@Valid @RequestBody SSHKeyRequestDto request) {
        return sshKeyService.createKey(request);
    }

    /** Omitting {@code privateKey}/{@code passphrase} keeps the stored secrets. */
    @PutMapping("/{sshKeyId}")
    public SSHKeyResponseDto updateKey(
            @PathVariable String sshKeyId, @Valid @RequestBody SSHKeyRequestDto request) {
        return sshKeyService.updateKey(sshKeyId, request);
    }

    @DeleteMapping("/{sshKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(@PathVariable String sshKeyId) {
        sshKeyService.deleteKey(sshKeyId);
    }
}
