from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.appdeployment import app_deployment_pb2 as _app_deployment_pb2
from org.apache.airavata.model.appcatalog.appinterface import app_interface_pb2 as _app_interface_pb2
from org.apache.airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterApplicationModuleRequest(_message.Message):
    __slots__ = ("gateway_id", "application_module")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_MODULE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    application_module: _app_deployment_pb2.ApplicationModule
    def __init__(self, gateway_id: _Optional[str] = ..., application_module: _Optional[_Union[_app_deployment_pb2.ApplicationModule, _Mapping]] = ...) -> None: ...

class RegisterApplicationModuleResponse(_message.Message):
    __slots__ = ("app_module_id",)
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    def __init__(self, app_module_id: _Optional[str] = ...) -> None: ...

class GetApplicationModuleRequest(_message.Message):
    __slots__ = ("app_module_id",)
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    def __init__(self, app_module_id: _Optional[str] = ...) -> None: ...

class UpdateApplicationModuleRequest(_message.Message):
    __slots__ = ("app_module_id", "application_module")
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_MODULE_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    application_module: _app_deployment_pb2.ApplicationModule
    def __init__(self, app_module_id: _Optional[str] = ..., application_module: _Optional[_Union[_app_deployment_pb2.ApplicationModule, _Mapping]] = ...) -> None: ...

class DeleteApplicationModuleRequest(_message.Message):
    __slots__ = ("app_module_id",)
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    def __init__(self, app_module_id: _Optional[str] = ...) -> None: ...

class GetAllAppModulesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllAppModulesResponse(_message.Message):
    __slots__ = ("application_modules",)
    APPLICATION_MODULES_FIELD_NUMBER: _ClassVar[int]
    application_modules: _containers.RepeatedCompositeFieldContainer[_app_deployment_pb2.ApplicationModule]
    def __init__(self, application_modules: _Optional[_Iterable[_Union[_app_deployment_pb2.ApplicationModule, _Mapping]]] = ...) -> None: ...

class GetAccessibleAppModulesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAccessibleAppModulesResponse(_message.Message):
    __slots__ = ("application_modules",)
    APPLICATION_MODULES_FIELD_NUMBER: _ClassVar[int]
    application_modules: _containers.RepeatedCompositeFieldContainer[_app_deployment_pb2.ApplicationModule]
    def __init__(self, application_modules: _Optional[_Iterable[_Union[_app_deployment_pb2.ApplicationModule, _Mapping]]] = ...) -> None: ...

class RegisterApplicationDeploymentRequest(_message.Message):
    __slots__ = ("gateway_id", "application_deployment")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_DEPLOYMENT_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    application_deployment: _app_deployment_pb2.ApplicationDeploymentDescription
    def __init__(self, gateway_id: _Optional[str] = ..., application_deployment: _Optional[_Union[_app_deployment_pb2.ApplicationDeploymentDescription, _Mapping]] = ...) -> None: ...

class RegisterApplicationDeploymentResponse(_message.Message):
    __slots__ = ("app_deployment_id",)
    APP_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    app_deployment_id: str
    def __init__(self, app_deployment_id: _Optional[str] = ...) -> None: ...

class GetApplicationDeploymentRequest(_message.Message):
    __slots__ = ("app_deployment_id",)
    APP_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    app_deployment_id: str
    def __init__(self, app_deployment_id: _Optional[str] = ...) -> None: ...

class UpdateApplicationDeploymentRequest(_message.Message):
    __slots__ = ("app_deployment_id", "application_deployment")
    APP_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_DEPLOYMENT_FIELD_NUMBER: _ClassVar[int]
    app_deployment_id: str
    application_deployment: _app_deployment_pb2.ApplicationDeploymentDescription
    def __init__(self, app_deployment_id: _Optional[str] = ..., application_deployment: _Optional[_Union[_app_deployment_pb2.ApplicationDeploymentDescription, _Mapping]] = ...) -> None: ...

class DeleteApplicationDeploymentRequest(_message.Message):
    __slots__ = ("app_deployment_id",)
    APP_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    app_deployment_id: str
    def __init__(self, app_deployment_id: _Optional[str] = ...) -> None: ...

class GetAllApplicationDeploymentsRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllApplicationDeploymentsResponse(_message.Message):
    __slots__ = ("application_deployments",)
    APPLICATION_DEPLOYMENTS_FIELD_NUMBER: _ClassVar[int]
    application_deployments: _containers.RepeatedCompositeFieldContainer[_app_deployment_pb2.ApplicationDeploymentDescription]
    def __init__(self, application_deployments: _Optional[_Iterable[_Union[_app_deployment_pb2.ApplicationDeploymentDescription, _Mapping]]] = ...) -> None: ...

class GetAccessibleApplicationDeploymentsRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAccessibleApplicationDeploymentsResponse(_message.Message):
    __slots__ = ("application_deployments",)
    APPLICATION_DEPLOYMENTS_FIELD_NUMBER: _ClassVar[int]
    application_deployments: _containers.RepeatedCompositeFieldContainer[_app_deployment_pb2.ApplicationDeploymentDescription]
    def __init__(self, application_deployments: _Optional[_Iterable[_Union[_app_deployment_pb2.ApplicationDeploymentDescription, _Mapping]]] = ...) -> None: ...

class GetAppModuleDeployedResourcesRequest(_message.Message):
    __slots__ = ("app_module_id",)
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    def __init__(self, app_module_id: _Optional[str] = ...) -> None: ...

class GetAppModuleDeployedResourcesResponse(_message.Message):
    __slots__ = ("compute_resource_ids",)
    COMPUTE_RESOURCE_IDS_FIELD_NUMBER: _ClassVar[int]
    compute_resource_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, compute_resource_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class GetDeploymentsForModuleAndProfileRequest(_message.Message):
    __slots__ = ("app_module_id", "group_resource_profile_id")
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    group_resource_profile_id: str
    def __init__(self, app_module_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ...) -> None: ...

class GetDeploymentsForModuleAndProfileResponse(_message.Message):
    __slots__ = ("application_deployments",)
    APPLICATION_DEPLOYMENTS_FIELD_NUMBER: _ClassVar[int]
    application_deployments: _containers.RepeatedCompositeFieldContainer[_app_deployment_pb2.ApplicationDeploymentDescription]
    def __init__(self, application_deployments: _Optional[_Iterable[_Union[_app_deployment_pb2.ApplicationDeploymentDescription, _Mapping]]] = ...) -> None: ...

class RegisterApplicationInterfaceRequest(_message.Message):
    __slots__ = ("gateway_id", "application_interface")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    application_interface: _app_interface_pb2.ApplicationInterfaceDescription
    def __init__(self, gateway_id: _Optional[str] = ..., application_interface: _Optional[_Union[_app_interface_pb2.ApplicationInterfaceDescription, _Mapping]] = ...) -> None: ...

class RegisterApplicationInterfaceResponse(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class CloneApplicationInterfaceRequest(_message.Message):
    __slots__ = ("app_interface_id", "new_application_name", "gateway_id")
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    NEW_APPLICATION_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    new_application_name: str
    gateway_id: str
    def __init__(self, app_interface_id: _Optional[str] = ..., new_application_name: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class CloneApplicationInterfaceResponse(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class GetApplicationInterfaceRequest(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class UpdateApplicationInterfaceRequest(_message.Message):
    __slots__ = ("app_interface_id", "application_interface")
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    application_interface: _app_interface_pb2.ApplicationInterfaceDescription
    def __init__(self, app_interface_id: _Optional[str] = ..., application_interface: _Optional[_Union[_app_interface_pb2.ApplicationInterfaceDescription, _Mapping]] = ...) -> None: ...

class DeleteApplicationInterfaceRequest(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class GetAllApplicationInterfaceNamesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllApplicationInterfaceNamesResponse(_message.Message):
    __slots__ = ("application_interface_names",)
    class ApplicationInterfaceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    APPLICATION_INTERFACE_NAMES_FIELD_NUMBER: _ClassVar[int]
    application_interface_names: _containers.ScalarMap[str, str]
    def __init__(self, application_interface_names: _Optional[_Mapping[str, str]] = ...) -> None: ...

class GetAllApplicationInterfacesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllApplicationInterfacesResponse(_message.Message):
    __slots__ = ("application_interfaces",)
    APPLICATION_INTERFACES_FIELD_NUMBER: _ClassVar[int]
    application_interfaces: _containers.RepeatedCompositeFieldContainer[_app_interface_pb2.ApplicationInterfaceDescription]
    def __init__(self, application_interfaces: _Optional[_Iterable[_Union[_app_interface_pb2.ApplicationInterfaceDescription, _Mapping]]] = ...) -> None: ...

class GetApplicationInputsRequest(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class GetApplicationInputsResponse(_message.Message):
    __slots__ = ("application_inputs",)
    APPLICATION_INPUTS_FIELD_NUMBER: _ClassVar[int]
    application_inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    def __init__(self, application_inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ...) -> None: ...

class GetApplicationOutputsRequest(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class GetApplicationOutputsResponse(_message.Message):
    __slots__ = ("application_outputs",)
    APPLICATION_OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    application_outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    def __init__(self, application_outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ...) -> None: ...

class GetAvailableComputeResourcesRequest(_message.Message):
    __slots__ = ("app_interface_id",)
    APP_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    app_interface_id: str
    def __init__(self, app_interface_id: _Optional[str] = ...) -> None: ...

class GetAvailableComputeResourcesResponse(_message.Message):
    __slots__ = ("compute_resource_names",)
    class ComputeResourceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    COMPUTE_RESOURCE_NAMES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_names: _containers.ScalarMap[str, str]
    def __init__(self, compute_resource_names: _Optional[_Mapping[str, str]] = ...) -> None: ...
