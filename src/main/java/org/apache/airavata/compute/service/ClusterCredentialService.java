package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.compute.dto.ClusterCredentialRequestDto;
import org.apache.airavata.compute.dto.ClusterCredentialResponseDto;
import org.apache.airavata.compute.mapper.ClusterCredentialMapper;
import org.apache.airavata.compute.model.ClusterCredentialEntity;
import org.apache.airavata.compute.model.ClusterEntity;
import org.apache.airavata.compute.repository.ClusterCredentialRepository;
import org.apache.airavata.compute.repository.ClusterRepository;
import org.apache.airavata.credentials.model.SSHUserCredential;
import org.apache.airavata.credentials.repository.SSHUserCredentialRepository;
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
 * CRUD for per-user SSH credential bindings on a Slurm cluster.
 *
 * <p>Ownership is never taken from client input: {@code createCredential} derives the
 * owning user from the caller's access token. Unlike the more permissive read model used
 * elsewhere (e.g. deployments, templates), single-record reads and all writes here are
 * restricted to the owning user or an admin — a credential binding ties a user's identity
 * to a specific SSH credential per cluster, which is more sensitive than the shared,
 * organization-level resources those other services expose.
 */
@Service
public class ClusterCredentialService {

    private final ClusterCredentialRepository credentialBindingRepository;
    private final ClusterRepository clusterRepository;
    private final SSHUserCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final ClusterCredentialMapper mapper;

    public ClusterCredentialService(
            ClusterCredentialRepository credentialBindingRepository,
            ClusterRepository clusterRepository,
            SSHUserCredentialRepository credentialRepository,
            UserRepository userRepository,
            ClusterCredentialMapper mapper) {
        this.credentialBindingRepository = credentialBindingRepository;
        this.clusterRepository = clusterRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /** Lists every binding across every user, optionally scoped to one cluster. */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<ClusterCredentialResponseDto> getAllCredentials(String clusterId) {
        List<ClusterCredentialEntity> entities = clusterId == null
                ? credentialBindingRepository.findAll()
                : credentialBindingRepository.findBySlurmCluster_ClusterId(clusterId);
        return entities.stream().map(mapper::toResponseDto).toList();
    }

    /** Lists the caller's own bindings, optionally scoped to one cluster. */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ClusterCredentialResponseDto> getMyCredentials(String clusterId) {
        String userId = resolveCurrentUser().getUserId();
        List<ClusterCredentialEntity> entities = clusterId == null
                ? credentialBindingRepository.findByUser_UserId(userId)
                : credentialBindingRepository.findByUser_UserIdAndSlurmCluster_ClusterId(userId, clusterId);
        return entities.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public ClusterCredentialResponseDto getCredential(String id) {
        ClusterCredentialEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        return mapper.toResponseDto(entity);
    }

    /** Any authenticated caller may bind a credential — this is a self-service setting. */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ClusterCredentialResponseDto createCredential(ClusterCredentialRequestDto request) {
        ClusterCredentialEntity entity = new ClusterCredentialEntity();
        entity.setSlurmCluster(resolveCluster(request.getClusterId()));
        entity.setSshUserCredential(resolveCredential(request.getSshCredentialId()));
        entity.setUser(resolveCurrentUser());
        return mapper.toResponseDto(credentialBindingRepository.save(entity));
    }

    /** Ownership is immutable — only the cluster/credential references can change. */
    @Transactional
    public ClusterCredentialResponseDto updateCredential(String id, ClusterCredentialRequestDto request) {
        ClusterCredentialEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        entity.setSlurmCluster(resolveCluster(request.getClusterId()));
        entity.setSshUserCredential(resolveCredential(request.getSshCredentialId()));
        return mapper.toResponseDto(credentialBindingRepository.save(entity));
    }

    @Transactional
    public void deleteCredential(String id) {
        ClusterCredentialEntity entity = findOrThrow(id);
        requireSelfOrAdmin(entity);
        credentialBindingRepository.delete(entity);
    }

    private ClusterCredentialEntity findOrThrow(String id) {
        return credentialBindingRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster credential binding not found: " + id));
    }

    private ClusterEntity resolveCluster(String clusterId) {
        return clusterRepository
                .findById(clusterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found: " + clusterId));
    }

    private SSHUserCredential resolveCredential(String credentialId) {
        return credentialRepository
                .findById(credentialId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "SSH credential not found: " + credentialId));
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

    private void requireSelfOrAdmin(ClusterCredentialEntity entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isSelf = entity.getUser() != null && authentication.getName().equals(entity.getUser().getUserId());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN") || auth.getAuthority().equals("SUPER_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new AccessDeniedException("Access denied: you may only access your own cluster credential bindings");
        }
    }
}
