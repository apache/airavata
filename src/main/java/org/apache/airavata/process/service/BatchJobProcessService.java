package org.apache.airavata.process.service;

import java.util.List;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;
import org.apache.airavata.application.repository.BatchApplicationDeploymentRepository;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.repository.UserRepository;
import org.apache.airavata.process.dto.BatchJobProcessRequestDto;
import org.apache.airavata.process.dto.BatchJobProcessResponseDto;
import org.apache.airavata.process.mapper.BatchJobProcessMapper;
import org.apache.airavata.process.model.BatchJobProcess;
import org.apache.airavata.process.repository.BatchJobProcessRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for batch job processes.
 *
 * <p>
 * Ownership is never taken from client input: {@code createProcess} derives the
 * owning
 * user from the caller's access token, so any authenticated caller can submit a
 * process
 * for themselves but can never submit one on someone else's behalf.
 */
@Service
public class BatchJobProcessService {

    private final BatchJobProcessRepository processRepository;
    private final BatchApplicationDeploymentRepository deploymentRepository;
    private final UserRepository userRepository;
    private final BatchJobProcessMapper mapper;

    public BatchJobProcessService(
            BatchJobProcessRepository processRepository,
            BatchApplicationDeploymentRepository deploymentRepository,
            UserRepository userRepository,
            BatchJobProcessMapper mapper) {
        this.processRepository = processRepository;
        this.deploymentRepository = deploymentRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /** Lists every process across every user. */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<BatchJobProcessResponseDto> getAllProcesses() {
        return processRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BatchJobProcessResponseDto> getProcessesByDeployment(String deploymentId) {
        return processRepository.findByBatchApplicationDeployment_DeploymentId(deploymentId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchJobProcessResponseDto getProcess(String processId) {
        return mapper.toResponseDto(findOrThrow(processId));
    }

    /**
     * Any authenticated caller may create a process — this is a self-service submission,
     * not an admin operation. The owning user is always the caller, taken from the access
     * token; the batch job config is taken from the request, letting a caller ask for
     * different resources than the deployment's own default.
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public BatchJobProcessResponseDto createProcess(BatchJobProcessRequestDto request) {
        BatchApplicationDeploymentEntity deployment = resolveDeployment(request.getDeploymentId());
        BatchJobProcess entity = mapper.toEntity(request);
        entity.setBatchApplicationDeployment(deployment);
        entity.setUser(resolveCurrentUser());
        return mapper.toResponseDto(processRepository.save(entity));
    }

    /**
     * Administrative correction of a process's deployment/batch job config. Ownership is
     * deliberately immutable here — re-deriving it from the caller's token on update would
     * reassign a process to whichever admin happens to issue the PUT.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public BatchJobProcessResponseDto updateProcess(String processId, BatchJobProcessRequestDto request) {
        BatchJobProcess entity = findOrThrow(processId);
        BatchApplicationDeploymentEntity deployment = resolveDeployment(request.getDeploymentId());
        mapper.updateEntity(request, entity);
        entity.setBatchApplicationDeployment(deployment);
        return mapper.toResponseDto(processRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteProcess(String processId) {
        processRepository.delete(findOrThrow(processId));
    }

    private BatchJobProcess findOrThrow(String processId) {
        return processRepository
                .findById(processId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found: " + processId));
    }

    private BatchApplicationDeploymentEntity resolveDeployment(String deploymentId) {
        return deploymentRepository
                .findById(deploymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Deployment not found: " + deploymentId));
    }

    /**
     * Resolves the caller's {@link UserEntity} from the principal name that
     * {@code UserRoleOpaqueTokenIntrospector} put on the security context — the
     * same
     * identifier used as {@code UserEntity.userId} (see
     * {@code UserService.getUserById}'s
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
}
