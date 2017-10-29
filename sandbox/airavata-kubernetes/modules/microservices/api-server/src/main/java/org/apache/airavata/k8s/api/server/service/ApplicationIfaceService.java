package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.application.ApplicationInput;
import org.apache.airavata.k8s.api.server.model.application.ApplicationInterface;
import org.apache.airavata.k8s.api.server.model.application.ApplicationOutput;
import org.apache.airavata.k8s.api.server.repository.ApplicationIfaceRepository;
import org.apache.airavata.k8s.api.server.repository.ApplicationInputRepository;
import org.apache.airavata.k8s.api.server.repository.ApplicationModuleRepository;
import org.apache.airavata.k8s.api.server.repository.ApplicationOutputRepository;
import org.apache.airavata.k8s.api.resources.application.ApplicationIfaceResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class ApplicationIfaceService {

    private ApplicationIfaceRepository ifaceRepository;
    private ApplicationInputRepository inputRepository;
    private ApplicationOutputRepository outputRepository;
    private ApplicationModuleRepository moduleRepository;

    @Autowired
    public ApplicationIfaceService(ApplicationIfaceRepository ifaceRepository,
                                   ApplicationInputRepository inputRepository,
                                   ApplicationOutputRepository outputRepository,
                                   ApplicationModuleRepository moduleRepository) {
        this.ifaceRepository = ifaceRepository;
        this.inputRepository = inputRepository;
        this.outputRepository = outputRepository;
        this.moduleRepository = moduleRepository;
    }

    public long create(ApplicationIfaceResource resource) {

        ApplicationInterface iface = new ApplicationInterface();
        iface.setName(resource.getName());
        iface.setDescription(resource.getDescription());

        iface.setApplicationModule(moduleRepository
                .findById(resource.getApplicationModuleId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find app module with id " +
                        resource.getApplicationModuleId())));

        Optional.ofNullable(resource.getInputs()).ifPresent(ips -> ips.forEach(ip -> {
            ApplicationInput appInput = new ApplicationInput();
            appInput.setName(ip.getName());
            appInput.setValue(ip.getValue());
            appInput.setArguments(ip.getArguments());
            appInput.setType(ApplicationInput.DataType.valueOf(ip.getType()));
            ApplicationInput saved = inputRepository.save(appInput);
            iface.getInputs().add(saved);
        }));

        Optional.ofNullable(resource.getOutputs()).ifPresent(ops -> ops.forEach(op -> {
            ApplicationOutput appOutput = new ApplicationOutput();
            appOutput.setName(op.getName());
            appOutput.setValue(op.getValue());
            appOutput.setType(ApplicationOutput.DataType.valueOf(op.getType()));
            ApplicationOutput saved = outputRepository.save(appOutput);
            iface.getOutputs().add(saved);
        }));

        ApplicationInterface saved = ifaceRepository.save(iface);
        return saved.getId();
    }

    public Optional<ApplicationIfaceResource> findById(long id) {
        return ToResourceUtil.toResource(ifaceRepository.findById(id).get());
    }

    public List<ApplicationIfaceResource> getAll() {
        List<ApplicationIfaceResource> computeList = new ArrayList<>();
        Optional.ofNullable(ifaceRepository.findAll())
                .ifPresent(computes ->
                        computes.forEach(compute -> computeList.add(ToResourceUtil.toResource(compute).get())));
        return computeList;
    }
}
