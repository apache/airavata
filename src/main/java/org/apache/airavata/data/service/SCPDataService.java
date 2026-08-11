package org.apache.airavata.data.service;

import java.util.List;
import org.apache.airavata.compute.model.ClusterCredentialEntity;
import org.apache.airavata.compute.repository.ClusterCredentialRepository;
import org.apache.airavata.data.dto.SCPDataRequestDto;
import org.apache.airavata.data.dto.SCPDataResponseDto;
import org.apache.airavata.data.mapper.SCPDataMapper;
import org.apache.airavata.data.model.DataProvisionStatus;
import org.apache.airavata.data.model.SCPDataEntity;
import org.apache.airavata.data.repository.SCPDataRepository;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for SCP data registrations.
 *
 * <p>Ownership is never taken from client input: {@code createData} derives the owning
 * user from the caller's access token. Single-record reads and all writes are restricted
 * to the owning user or an admin, matching the model used by
 * {@code ClusterCredentialService} — a data registration is a per-user resource, not
 * a shared, organization-level one.
 */
@Service
public class SCPDataService {

    private final SCPDataRepository dataRepository;
    private final ClusterCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final SCPDataMapper mapper;

    public SCPDataService(
            SCPDataRepository dataRepository,
            ClusterCredentialRepository credentialRepository,
            UserRepository userRepository,
            SCPDataMapper mapper) {
        this.dataRepository = dataRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /** Lists every data registration across every user. */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<SCPDataResponseDto> getAllData() {
        return dataRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    /** Lists the caller's own data registrations. */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<SCPDataResponseDto> getMyData() {
        String userId = resolveCurrentUser().getUserId();
        return dataRepository.findByOwner_UserId(userId).stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public SCPDataResponseDto getData(String id) {
        SCPDataEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        return mapper.toResponseDto(entity);
    }

    /** Any authenticated caller may register data — this is a self-service action. */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public SCPDataResponseDto createData(SCPDataRequestDto request) {
        SCPDataEntity entity = mapper.toEntity(request);
        entity.setSlurmClusterCredential(resolveCredential(request.getSlurmClusterCredentialId()));
        entity.setOwner(resolveCurrentUser());
        entity.setProvisionStatus(DataProvisionStatus.REGISTERD);
        return mapper.toResponseDto(dataRepository.save(entity));
    }

    /** Ownership is immutable — only the descriptive fields and credential can change. */
    @Transactional
    public SCPDataResponseDto updateData(String id, SCPDataRequestDto request) {
        SCPDataEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        mapper.updateEntity(request, entity);
        entity.setSlurmClusterCredential(resolveCredential(request.getSlurmClusterCredentialId()));
        return mapper.toResponseDto(dataRepository.save(entity));
    }

    @Transactional
    public void deleteData(String id) {
        SCPDataEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        dataRepository.delete(entity);
    }

    private SCPDataEntity findOrThrow(String id) {
        return dataRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SCP data not found: " + id));
    }

    private ClusterCredentialEntity resolveCredential(String credentialId) {
        return credentialRepository
                .findById(credentialId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Slurm cluster credential not found: " + credentialId));
    }

    /**
     * Resolves the caller's {@link UserEntity} from the principal name that
     * {@code UserRoleOpaqueTokenIntrospector} put on the security context — the same
     * identifier used as {@code UserEntity.userId} (see {@code UserService.getUserById}'s
     * "isSelf" check for the other place this equivalence is relied on).
     */
    private UserEntity resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository
                .findById(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No user record found for authenticated principal: " + username));
    }

    private void requireSelfOrAdmin(SCPDataEntity entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isSelf = entity.getOwner() != null && authentication.getName().equals(entity.getOwner().getUserId());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN") || auth.getAuthority().equals("SUPER_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new AccessDeniedException("Access denied: you may only access your own SCP data");
        }
    }
}
