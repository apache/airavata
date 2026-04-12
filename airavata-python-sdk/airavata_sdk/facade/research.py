import importlib

from google.protobuf.struct_pb2 import Struct

from airavata_sdk.transport.utils import (
    create_experiment_service_stub,
    create_project_service_stub,
    create_application_catalog_service_stub,
    create_parser_service_stub,
    create_data_product_service_stub,
    create_notification_service_stub,
    create_experiment_management_service_stub,
    create_research_hub_service_stub,
    create_research_project_service_stub,
    create_research_resource_service_stub,
    create_research_session_service_stub,
)


class ResearchClient:
    """Experiments, projects, application catalog, parsers, data products,
    notifications, experiment management, research hub/project/resource/session."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._experiment = create_experiment_service_stub(channel)
        self._project = create_project_service_stub(channel)
        self._app_catalog = create_application_catalog_service_stub(channel)
        self._parser = create_parser_service_stub(channel)
        self._data_product = create_data_product_service_stub(channel)
        self._notification = create_notification_service_stub(channel)
        self._exp_mgmt = create_experiment_management_service_stub(channel)
        self._research_hub = create_research_hub_service_stub(channel)
        self._research_project = create_research_project_service_stub(channel)
        self._research_resource = create_research_resource_service_stub(channel)
        self._research_session = create_research_session_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ================================================================
    # Experiment Service
    # ================================================================

    def search_experiments(self, gateway_id, user_name, filters=None, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.SearchExperiments(
            pb2.SearchExperimentsRequest(gateway_id=gateway_id, user_name=user_name, filters=filters or {}, limit=limit, offset=offset),
            metadata=self._metadata,
        )
        return list(response.experiments)

    def get_experiment_statistics(self, gateway_id, from_time, to_time, user_name="", application_name="", resource_host_name="", limit=0, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentStatistics(
            pb2.GetExperimentStatisticsRequest(
                gateway_id=gateway_id, from_time=from_time, to_time=to_time,
                user_name=user_name, application_name=application_name,
                resource_host_name=resource_host_name, limit=limit, offset=offset,
            ),
            metadata=self._metadata,
        )

    def get_experiments_in_project(self, project_id, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.GetExperimentsInProject(
            pb2.GetExperimentsInProjectRequest(project_id=project_id, limit=limit, offset=offset),
            metadata=self._metadata,
        )
        return list(response.experiments)

    def get_user_experiments(self, gateway_id, user_name, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.GetUserExperiments(
            pb2.GetUserExperimentsRequest(gateway_id=gateway_id, user_name=user_name, limit=limit, offset=offset),
            metadata=self._metadata,
        )
        return list(response.experiments)

    def create_experiment(self, gateway_id, experiment):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.CreateExperiment(
            pb2.CreateExperimentRequest(gateway_id=gateway_id, experiment=experiment),
            metadata=self._metadata,
        )
        return response.experiment_id

    def delete_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.DeleteExperiment(
            pb2.DeleteExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperiment(
            pb2.GetExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment_by_admin(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentByAdmin(
            pb2.GetExperimentByAdminRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_detailed_experiment_tree(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetDetailedExperimentTree(
            pb2.GetDetailedExperimentTreeRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def update_experiment(self, experiment_id, experiment):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateExperiment(
            pb2.UpdateExperimentRequest(experiment_id=experiment_id, experiment=experiment),
            metadata=self._metadata,
        )

    def update_experiment_configuration(self, experiment_id, configuration):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateExperimentConfiguration(
            pb2.UpdateExperimentConfigurationRequest(experiment_id=experiment_id, configuration=configuration),
            metadata=self._metadata,
        )

    def update_resource_scheduling(self, experiment_id, scheduling):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateResourceScheduling(
            pb2.UpdateResourceSchedulingRequest(experiment_id=experiment_id, scheduling=scheduling),
            metadata=self._metadata,
        )

    def validate_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.ValidateExperiment(
            pb2.ValidateExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def launch_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.LaunchExperiment(
            pb2.LaunchExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_experiment_status(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentStatus(
            pb2.GetExperimentStatusRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment_outputs(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.GetExperimentOutputs(
            pb2.GetExperimentOutputsRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )
        return list(response.outputs)

    def get_intermediate_outputs(self, experiment_id, output_names=None):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.FetchIntermediateOutputs(
            pb2.FetchIntermediateOutputsRequest(experiment_id=experiment_id, output_names=output_names or []),
            metadata=self._metadata,
        )

    def get_intermediate_output_process_status(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetIntermediateOutputProcessStatus(
            pb2.GetIntermediateOutputProcessStatusRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_job_statuses(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetJobStatuses(
            pb2.GetJobStatusesRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_job_details(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.GetJobDetails(
            pb2.GetJobDetailsRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )
        return list(response.jobs)

    def clone_experiment(self, experiment_id, new_experiment_name="", new_experiment_project_id=""):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.CloneExperiment(
            pb2.CloneExperimentRequest(
                experiment_id=experiment_id,
                new_experiment_name=new_experiment_name,
                new_experiment_project_id=new_experiment_project_id,
            ),
            metadata=self._metadata,
        )
        return response.experiment_id

    def clone_experiment_by_admin(self, experiment_id, new_experiment_name="", new_experiment_project_id=""):
        pb2 = self._svc("experiment_service_pb2")
        response = self._experiment.CloneExperiment(
            pb2.CloneExperimentRequest(
                experiment_id=experiment_id,
                new_experiment_name=new_experiment_name,
                new_experiment_project_id=new_experiment_project_id,
            ),
            metadata=self._metadata,
        )
        return response.experiment_id

    def terminate_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.TerminateExperiment(
            pb2.TerminateExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Project Service
    # ================================================================

    def create_project(self, gateway_id, project):
        pb2 = self._svc("project_service_pb2")
        response = self._project.CreateProject(
            pb2.CreateProjectRequest(gateway_id=gateway_id, project=project),
            metadata=self._metadata,
        )
        return response.project_id

    def update_project(self, project_id, project):
        pb2 = self._svc("project_service_pb2")
        return self._project.UpdateProject(
            pb2.UpdateProjectRequest(project_id=project_id, project=project),
            metadata=self._metadata,
        )

    def get_project(self, project_id):
        pb2 = self._svc("project_service_pb2")
        return self._project.GetProject(
            pb2.GetProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )

    def delete_project(self, project_id):
        pb2 = self._svc("project_service_pb2")
        return self._project.DeleteProject(
            pb2.DeleteProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )

    def get_user_projects(self, gateway_id, user_name, limit=-1, offset=0):
        pb2 = self._svc("project_service_pb2")
        response = self._project.GetUserProjects(
            pb2.GetUserProjectsRequest(gateway_id=gateway_id, user_name=user_name, limit=limit, offset=offset),
            metadata=self._metadata,
        )
        return list(response.projects)

    def search_projects(self, gateway_id, user_name, filters=None, limit=-1, offset=0):
        pb2 = self._svc("project_service_pb2")
        response = self._project.SearchProjects(
            pb2.SearchProjectsRequest(gateway_id=gateway_id, user_name=user_name, filters=filters or {}, limit=limit, offset=offset),
            metadata=self._metadata,
        )
        return list(response.projects)

    # ================================================================
    # Application Catalog Service
    # ================================================================

    def register_application_module(self, gateway_id, application_module):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.RegisterApplicationModule(
            pb2.RegisterApplicationModuleRequest(gateway_id=gateway_id, application_module=application_module),
            metadata=self._metadata,
        )
        return response.app_module_id

    def get_application_module(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationModule(
            pb2.GetApplicationModuleRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )

    def update_application_module(self, app_module_id, application_module):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationModule(
            pb2.UpdateApplicationModuleRequest(app_module_id=app_module_id, application_module=application_module),
            metadata=self._metadata,
        )

    def get_all_app_modules(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAllAppModules(
            pb2.GetAllAppModulesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.application_modules)

    def get_accessible_app_modules(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAccessibleAppModules(
            pb2.GetAccessibleAppModulesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.application_modules)

    def delete_application_module(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationModule(
            pb2.DeleteApplicationModuleRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )

    def register_application_deployment(self, gateway_id, application_deployment):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.RegisterApplicationDeployment(
            pb2.RegisterApplicationDeploymentRequest(gateway_id=gateway_id, application_deployment=application_deployment),
            metadata=self._metadata,
        )
        return response.app_deployment_id

    def get_application_deployment(self, app_deployment_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationDeployment(
            pb2.GetApplicationDeploymentRequest(app_deployment_id=app_deployment_id),
            metadata=self._metadata,
        )

    def update_application_deployment(self, app_deployment_id, application_deployment):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationDeployment(
            pb2.UpdateApplicationDeploymentRequest(app_deployment_id=app_deployment_id, application_deployment=application_deployment),
            metadata=self._metadata,
        )

    def delete_application_deployment(self, app_deployment_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationDeployment(
            pb2.DeleteApplicationDeploymentRequest(app_deployment_id=app_deployment_id),
            metadata=self._metadata,
        )

    def get_all_application_deployments(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAllApplicationDeployments(
            pb2.GetAllApplicationDeploymentsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.application_deployments)

    def get_accessible_application_deployments(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAccessibleApplicationDeployments(
            pb2.GetAccessibleApplicationDeploymentsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.application_deployments)

    def get_app_module_deployed_resources(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAppModuleDeployedResources(
            pb2.GetAppModuleDeployedResourcesRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )
        return list(response.compute_resource_ids)

    def get_application_deployments_for_app_module_and_group_resource_profile(self, app_module_id, group_resource_profile_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetDeploymentsForModuleAndProfile(
            pb2.GetDeploymentsForModuleAndProfileRequest(app_module_id=app_module_id, group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )
        return list(response.application_deployments)

    def register_application_interface(self, gateway_id, application_interface):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.RegisterApplicationInterface(
            pb2.RegisterApplicationInterfaceRequest(gateway_id=gateway_id, application_interface=application_interface),
            metadata=self._metadata,
        )
        return response.app_interface_id

    def clone_application_interface(self, existing_app_interface_id, new_app_module_name, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.CloneApplicationInterface(
            pb2.CloneApplicationInterfaceRequest(
                existing_app_interface_id=existing_app_interface_id,
                new_app_module_name=new_app_module_name,
                gateway_id=gateway_id,
            ),
            metadata=self._metadata,
        )
        return response.app_interface_id

    def get_application_interface(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationInterface(
            pb2.GetApplicationInterfaceRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def update_application_interface(self, app_interface_id, application_interface):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationInterface(
            pb2.UpdateApplicationInterfaceRequest(app_interface_id=app_interface_id, application_interface=application_interface),
            metadata=self._metadata,
        )

    def delete_application_interface(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationInterface(
            pb2.DeleteApplicationInterfaceRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def get_all_application_interface_names(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAllApplicationInterfaceNames(
            pb2.GetAllApplicationInterfaceNamesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return dict(response.application_interface_names)

    def get_all_application_interfaces(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAllApplicationInterfaces(
            pb2.GetAllApplicationInterfacesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.application_interfaces)

    def get_application_inputs(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetApplicationInputs(
            pb2.GetApplicationInputsRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )
        return list(response.application_inputs)

    def get_application_outputs(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetApplicationOutputs(
            pb2.GetApplicationOutputsRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )
        return list(response.application_outputs)

    def get_available_app_interface_compute_resources(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        response = self._app_catalog.GetAvailableComputeResources(
            pb2.GetAvailableComputeResourcesRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )
        return dict(response.compute_resource_names)

    # ================================================================
    # Parser Service
    # ================================================================

    def get_parser(self, parser_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.GetParser(
            pb2.GetParserRequest(parser_id=parser_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def save_parser(self, parser):
        pb2 = self._svc("parser_service_pb2")
        response = self._parser.SaveParser(
            pb2.SaveParserRequest(parser=parser),
            metadata=self._metadata,
        )
        return response.parser_id

    def list_all_parsers(self, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        response = self._parser.ListAllParsers(
            pb2.ListAllParsersRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.parsers)

    def remove_parser(self, parser_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.RemoveParser(
            pb2.RemoveParserRequest(parser_id=parser_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_parsing_template(self, template_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.GetParsingTemplate(
            pb2.GetParsingTemplateRequest(template_id=template_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_parsing_templates_for_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        response = self._parser.GetParsingTemplatesForExperiment(
            pb2.GetParsingTemplatesForExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.parsing_templates)

    def save_parsing_template(self, parsing_template):
        pb2 = self._svc("parser_service_pb2")
        response = self._parser.SaveParsingTemplate(
            pb2.SaveParsingTemplateRequest(parsing_template=parsing_template),
            metadata=self._metadata,
        )
        return response.template_id

    def remove_parsing_template(self, template_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.RemoveParsingTemplate(
            pb2.RemoveParsingTemplateRequest(template_id=template_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def list_all_parsing_templates(self, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        response = self._parser.ListAllParsingTemplates(
            pb2.ListAllParsingTemplatesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.parsing_templates)

    # ================================================================
    # Data Product Service
    # ================================================================

    def register_data_product(self, data_product):
        pb2 = self._svc("data_product_service_pb2")
        response = self._data_product.RegisterDataProduct(
            pb2.RegisterDataProductRequest(data_product=data_product),
            metadata=self._metadata,
        )
        return response.product_uri

    def get_data_product(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetDataProduct(
            pb2.GetDataProductRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )

    def register_replica_location(self, replica_location):
        pb2 = self._svc("data_product_service_pb2")
        response = self._data_product.RegisterReplicaLocation(
            pb2.RegisterReplicaLocationRequest(replica_location=replica_location),
            metadata=self._metadata,
        )
        return response.replica_id

    def get_parent_data_product(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetParentDataProduct(
            pb2.GetParentDataProductRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )

    def get_child_data_products(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        response = self._data_product.GetChildDataProducts(
            pb2.GetChildDataProductsRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )
        return list(response.data_products)

    def update_data_product(self, product_uri, data_product):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.UpdateDataProduct(
            pb2.UpdateDataProductRequest(product_uri=product_uri, data_product=data_product),
            metadata=self._metadata,
        )

    def delete_data_product(self, product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.DeleteDataProduct(
            pb2.DeleteDataProductRequest(product_uri=product_uri),
            metadata=self._metadata,
        )

    def get_replica_location(self, replica_id):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetReplicaLocation(
            pb2.GetReplicaLocationRequest(replica_id=replica_id),
            metadata=self._metadata,
        )

    def update_replica_location(self, replica_id, replica_location):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.UpdateReplicaLocation(
            pb2.UpdateReplicaLocationRequest(replica_id=replica_id, replica_location=replica_location),
            metadata=self._metadata,
        )

    def delete_replica_location(self, replica_id):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.DeleteReplicaLocation(
            pb2.DeleteReplicaLocationRequest(replica_id=replica_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Notification Service
    # ================================================================

    def create_notification(self, notification):
        pb2 = self._svc("notification_service_pb2")
        response = self._notification.CreateNotification(
            pb2.CreateNotificationRequest(notification=notification),
            metadata=self._metadata,
        )
        return response.notification_id

    def update_notification(self, notification):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.UpdateNotification(
            pb2.UpdateNotificationRequest(notification=notification),
            metadata=self._metadata,
        )

    def delete_notification(self, gateway_id, notification_id):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.DeleteNotification(
            pb2.DeleteNotificationRequest(gateway_id=gateway_id, notification_id=notification_id),
            metadata=self._metadata,
        )

    def get_notification(self, gateway_id, notification_id):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.GetNotification(
            pb2.GetNotificationRequest(gateway_id=gateway_id, notification_id=notification_id),
            metadata=self._metadata,
        )

    def get_all_notifications(self, gateway_id):
        pb2 = self._svc("notification_service_pb2")
        response = self._notification.GetAllNotifications(
            pb2.GetAllNotificationsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.notifications)

    # ================================================================
    # Experiment Management Service
    # ================================================================

    def get_agent_experiment(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._exp_mgmt.GetExperiment(
            pb2.GetAgentExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def launch_agent_experiment(
        self, experiment_name: str, project_name: str, remote_cluster: str,
        group: str = "", libraries: list[str] = None, pip: list[str] = None,
        mounts: list[str] = None, queue: str = "", wall_time: int = 0,
        cpu_count: int = 0, node_count: int = 0, memory: int = 0,
        input_storage_id: str = "", output_storage_id: str = "",
    ):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._exp_mgmt.LaunchExperiment(
            pb2.AgentLaunchRequest(
                experiment_name=experiment_name, project_name=project_name,
                remote_cluster=remote_cluster, group=group,
                libraries=libraries or [], pip=pip or [],
                mounts=mounts or [], queue=queue, wall_time=wall_time,
                cpu_count=cpu_count, node_count=node_count, memory=memory,
                input_storage_id=input_storage_id, output_storage_id=output_storage_id,
            ),
            metadata=self._metadata,
        )

    def launch_optimized_experiment(self, requests: list):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._exp_mgmt.LaunchOptimizedExperiment(
            pb2.LaunchOptimizedExperimentRequest(requests=requests),
            metadata=self._metadata,
        )

    def terminate_agent_experiment(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._exp_mgmt.TerminateExperiment(
            pb2.TerminateAgentExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_process_model(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._exp_mgmt.GetProcessModel(
            pb2.GetProcessModelRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Research Hub Service
    # ================================================================

    def start_project_session(self, project_id: str, session_name: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_hub.StartProjectSession(
            pb2.StartProjectSessionRequest(project_id=project_id, session_name=session_name),
            metadata=self._metadata,
        )

    def resume_session(self, session_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_hub.ResumeSession(
            pb2.ResumeSessionRequest(session_id=session_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Research Project Service
    # ================================================================

    def get_all_research_projects(self):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_project.GetAllProjects(
            pb2.GetAllProjectsRequest(),
            metadata=self._metadata,
        )

    def get_research_projects_by_owner(self, owner_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_project.GetProjectsByOwner(
            pb2.GetProjectsByOwnerRequest(owner_id=owner_id),
            metadata=self._metadata,
        )

    def create_research_project(self, name: str, owner_id: str, repository_id: str = "", dataset_ids: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_project.CreateProject(
            pb2.CreateResearchProjectRequest(
                name=name, owner_id=owner_id,
                repository_id=repository_id, dataset_ids=dataset_ids or [],
            ),
            metadata=self._metadata,
        )

    def delete_research_project(self, project_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_project.DeleteProject(
            pb2.DeleteProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Research Resource Service
    # ================================================================

    def create_dataset(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._research_resource.CreateDataset(
            struct,
            metadata=self._metadata,
        )

    def create_notebook(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._research_resource.CreateNotebook(
            struct,
            metadata=self._metadata,
        )

    def create_repository(self, name: str, description: str = "", header_image: str = "",
                          tags: list[str] = None, authors: list[str] = None,
                          privacy: str = "PUBLIC", github_url: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        resource = pb2.CreateResourceRequest(
            name=name, description=description, header_image=header_image,
            tags=tags or [], authors=authors or [], privacy=privacy,
        )
        return self._research_resource.CreateRepository(
            pb2.CreateRepositoryResourceRequest(resource=resource, github_url=github_url),
            metadata=self._metadata,
        )

    def modify_repository(self, id: str, name: str = "", description: str = "",
                          header_image: str = "", tags: list[str] = None,
                          authors: list[str] = None, privacy: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.ModifyRepository(
            pb2.ModifyResourceRequest(
                id=id, name=name, description=description,
                header_image=header_image, tags=tags or [],
                authors=authors or [], privacy=privacy,
            ),
            metadata=self._metadata,
        )

    def create_model(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._research_resource.CreateModel(
            struct,
            metadata=self._metadata,
        )

    def get_tags(self, page_number: int = 0, page_size: int = 0, name_search: str = "",
                 types: list[str] = None, tags: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetTags(
            pb2.GetAllResourcesRequest(
                page_number=page_number, page_size=page_size,
                name_search=name_search, types=types or [], tags=tags or [],
            ),
            metadata=self._metadata,
        )

    def get_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    def delete_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.DeleteResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    def get_all_resources(self, page_number: int = 0, page_size: int = 0, name_search: str = "",
                          types: list[str] = None, tags: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetAllResources(
            pb2.GetAllResourcesRequest(
                page_number=page_number, page_size=page_size,
                name_search=name_search, types=types or [], tags=tags or [],
            ),
            metadata=self._metadata,
        )

    def search_resources(self, type: str, name: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.SearchResources(
            pb2.SearchResourceRequest(type=type, name=name),
            metadata=self._metadata,
        )

    def get_projects_for_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetProjectsForResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    def star_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.StarResource(
            pb2.StarResourceRequest(id=id),
            metadata=self._metadata,
        )

    def check_user_starred_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.CheckUserStarredResource(
            pb2.StarResourceRequest(id=id),
            metadata=self._metadata,
        )

    def get_resource_star_count(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetResourceStarCount(
            pb2.GetResourceStarCountRequest(id=id),
            metadata=self._metadata,
        )

    def get_starred_resources(self, user_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_resource.GetStarredResources(
            pb2.GetStarredResourcesRequest(user_id=user_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Research Session Service
    # ================================================================

    def get_sessions(self, status: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_session.GetSessions(
            pb2.GetSessionsRequest(status=status),
            metadata=self._metadata,
        )

    def update_session_status(self, session_id: str, status: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_session.UpdateSessionStatus(
            pb2.UpdateSessionStatusRequest(session_id=session_id, status=status),
            metadata=self._metadata,
        )

    def delete_session(self, session_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_session.DeleteSession(
            pb2.DeleteSessionRequest(session_id=session_id),
            metadata=self._metadata,
        )

    def delete_sessions(self, session_ids: list[str]):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._research_session.DeleteSessions(
            pb2.DeleteSessionsRequest(session_ids=session_ids),
            metadata=self._metadata,
        )
