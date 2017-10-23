package org.apache.airavata.k8s.api.server.service.util;

import org.apache.airavata.k8s.api.server.model.application.ApplicationDeployment;
import org.apache.airavata.k8s.api.server.model.application.ApplicationInterface;
import org.apache.airavata.k8s.api.server.model.application.ApplicationModule;
import org.apache.airavata.k8s.api.server.model.application.ComputeResourceModel;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.resources.application.*;
import org.apache.airavata.k8s.api.server.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.server.resources.experiment.ExperimentResource;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ToResourceUtil {

    public static Optional<ExperimentResource> toResource(Experiment experiment) {

        if (experiment != null) {
            ExperimentResource resource = new ExperimentResource();
            resource.setId(resource.getId());
            resource.setExperimentName(experiment.getExperimentName());
            resource.setDescription(experiment.getDescription());
            Optional.ofNullable(experiment.getErrors())
                    .ifPresent(errs -> errs.forEach(err -> resource.getErrorsIds().add(err.getId())));
            Optional.ofNullable(experiment.getExperimentStatus())
                    .ifPresent(sts -> sts.forEach(status -> resource.getExperimentStatusIds().add(status.getId())));
            return Optional.of(resource);

        } else {
            return Optional.empty();
        }
    }

    public static Optional<ComputeResource> toResource(ComputeResourceModel computeResourceModel) {

        if (computeResourceModel != null) {
            ComputeResource resource = new ComputeResource();
            resource.setId(computeResourceModel.getId());
            resource.setName(computeResourceModel.getName());
            return Optional.of(resource);

        } else {
            return Optional.empty();
        }
    }

    public static Optional<ApplicationModuleResource> toResource(ApplicationModule applicationModule) {

        if (applicationModule != null) {
            ApplicationModuleResource resource = new ApplicationModuleResource();
            resource.setId(applicationModule.getId());
            resource.setName(applicationModule.getName());
            resource.setDescription(applicationModule.getDescription());
            resource.setVersion(applicationModule.getVersion());
            return Optional.of(resource);

        } else {
            return Optional.empty();
        }
    }

    public static Optional<ApplicationDeploymentResource> toResource(ApplicationDeployment applicationDeployment) {
        if (applicationDeployment != null) {
            ApplicationDeploymentResource resource = new ApplicationDeploymentResource();
            resource.setId(applicationDeployment.getId());
            resource.setExecutablePath(applicationDeployment.getExecutablePath());
            resource.setPreJobCommand(applicationDeployment.getPreJobCommand());
            resource.setPostJobCommand(resource.getPostJobCommand());
            Optional.ofNullable(applicationDeployment.getApplicationModule())
                    .ifPresent(module -> resource.setApplicationModuleId(module.getId()));
            Optional.ofNullable(applicationDeployment.getComputeResource())
                    .ifPresent(cr -> resource.setComputeResourceId(cr.getId()));
            return Optional.of(resource);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<ApplicationIfaceResource> toResource(ApplicationInterface applicationInterface) {
        if (applicationInterface != null) {
            ApplicationIfaceResource resource = new ApplicationIfaceResource();
            resource.setId(applicationInterface.getId());
            resource.setName(applicationInterface.getName());
            resource.setApplicationModuleId(applicationInterface.getId());
            resource.setDescription(applicationInterface.getDescription());

            Optional.ofNullable(applicationInterface.getInputs()).ifPresent(ips -> ips.forEach(ip -> {
                ApplicationInputResource ipResource = new ApplicationInputResource();
                ipResource.setId(ip.getId());
                ipResource.setName(ip.getName());
                ipResource.setArguments(ip.getArguments());
                ipResource.setType(ip.getType().getValue());
                ipResource.setValue(ip.getValue());
                resource.getInputs().add(ipResource);
            }));

            Optional.ofNullable(applicationInterface.getOutputs()).ifPresent(ops -> ops.forEach(op -> {
                ApplicationOutputResource opResource = new ApplicationOutputResource();
                opResource.setId(op.getId());
                opResource.setName(op.getName());
                opResource.setType(op.getType().getValue());
                opResource.setValue(op.getValue());
                resource.getOutputs().add(opResource);
            }));

            return Optional.of(resource);
        } else {
            return Optional.empty();
        }
    }
}
