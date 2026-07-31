package org.apache.airavata.application.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.application.mapper.ApplicationMapper;
import org.apache.airavata.application.model.ApplicationModuleEntity;
import org.apache.airavata.application.repository.ApplicationModuleRepository;
import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.common.RequestContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;

@Component
public class ApplicationModuleService {

    private final ApplicationModuleRepository applicationModuleRepository;

    public ApplicationModuleService(ApplicationModuleRepository applicationModuleRepository) {
        this.applicationModuleRepository = applicationModuleRepository;
    }

    public ApplicationModule getApplicationModule(RequestContext requestContext, String moduleId) throws Exception {
        ApplicationModuleEntity entity = applicationModuleRepository.findById(moduleId)
                .orElseThrow(() -> new Exception("Application module not found: " + moduleId));
        return toModel(entity);
    }

    public List<ApplicationModule> getAllApplicationModules() throws Exception {
        List<ApplicationModule> modules = new ArrayList<>();
        applicationModuleRepository.findAll().forEach(entity -> modules.add(toModel(entity)));
        return modules;
    }

    public ApplicationModule createApplicationModule(RequestContext requestContext, ApplicationModule module)
            throws Exception {
        String moduleId = module.appModuleId() == null || module.appModuleId().isBlank()
                ? AiravataUtils.getId(module.appModuleName())
                : module.appModuleId();
        ApplicationModuleEntity entity = toEntity(module.toBuilder().setAppModuleId(moduleId).build());
        entity.setGatewayId("default"); // TODO: Set with defaiult gatewayId from requestContext when available
        Timestamp now = new Timestamp(System.currentTimeMillis());
        entity.setCreationTime(now);
        entity.setUpdateTime(now);
        ApplicationModuleEntity savedEntity = applicationModuleRepository.save(entity);
        return toModel(savedEntity);
    }

    public ApplicationModule updateApplicationModule(RequestContext requestContext, String moduleId,
            ApplicationModule module) throws Exception {
        ApplicationModuleEntity existingEntity = applicationModuleRepository.findById(moduleId)
                .orElseThrow(() -> new Exception("Application module not found: " + moduleId));
        ApplicationModuleEntity updatedEntity = toEntity(module);
        updatedEntity.setAppModuleId(existingEntity.getAppModuleId()); // Preserve the original ID
        ApplicationModuleEntity savedEntity = applicationModuleRepository.save(updatedEntity);
        return toModel(savedEntity);
    }

    // Demo of the JWT -> role-lookup -> @PreAuthorize wiring: deleting a module now
    // requires the caller's bearer token to resolve to the ADMIN authority.
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteApplicationModule(RequestContext requestContext, String moduleId) throws Exception {
        if (!applicationModuleRepository.existsById(moduleId)) {
            throw new Exception("Application module not found: " + moduleId);
        }
        applicationModuleRepository.deleteById(moduleId);
    }

    protected ApplicationModule toModel(ApplicationModuleEntity entity) {
        return ApplicationMapper.INSTANCE.appModuleToModel(entity);
    }

    protected ApplicationModuleEntity toEntity(ApplicationModule model) {
        return ApplicationMapper.INSTANCE.appModuleToEntity(model);
    }

}
