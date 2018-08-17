import UnhandledError from './errors/UnhandledError'
import UnhandledErrorDispatcher from './errors/UnhandledErrorDispatcher'
import UnhandledErrorDisplayList from './errors/UnhandledErrorDisplayList'

import ApplicationInterfaceDefinition from './models/ApplicationInterfaceDefinition'
import ApplicationModule from './models/ApplicationModule'
import BatchQueue from './models/BatchQueue'
import BatchQueueResourcePolicy from './models/BatchQueueResourcePolicy'
import ComputeResourcePolicy from './models/ComputeResourcePolicy'
import DataType from './models/DataType'
import Experiment from './models/Experiment'
import ExperimentState from './models/ExperimentState'
import FullExperiment from './models/FullExperiment'
import Group from './models/Group'
import GroupComputeResourcePreference from './models/GroupComputeResourcePreference'
import GroupPermission from './models/GroupPermission'
import GroupResourceProfile from './models/GroupResourceProfile'
import InputDataObjectType from './models/InputDataObjectType'
import OutputDataObjectType from './models/OutputDataObjectType'
import Project from './models/Project'
import ResourcePermissionType from './models/ResourcePermissionType'
import SharedEntity from './models/SharedEntity'
import SummaryType from './models/SummaryType'
import UserPermission from './models/UserPermission'

import ApplicationDeploymentService from './services/ApplicationDeploymentService'
import ApplicationInterfaceService from './services/ApplicationInterfaceService'
import ApplicationModuleService from './services/ApplicationModuleService'
import ExperimentService from './services/ExperimentService'
import ExperimentSearchService from './services/ExperimentSearchService'
import FullExperimentService from './services/FullExperimentService'
import ProjectService from './services/ProjectService'
import GroupService from './services/GroupService'
import UserProfileService from './services/UserProfileService'
import CloudJobSubmissionService from './services/CloudJobSubmissionService'
import GlobusJobSubmissionService from './services/GlobusJobSubmissionService'
import LocaJobSubmissionService from './services/LocaJobSubmissionService'
import SshJobSubmissionService from './services/SshJobSubmissionService'
import UnicoreJobSubmissionService from './services/UnicoreJobSubmissionService'
import SCPDataMovementService from './services/SCPDataMovementService'
import GridFTPDataMovementService from './services/GridFTPDataMovementService'
import UnicoreDataMovementService from './services/UnicoreDataMovementService'
import ServiceFactory from './services/ServiceFactory'
import FetchUtils from './utils/FetchUtils'
import PaginationIterator from './utils/PaginationIterator'

exports.errors = {
    UnhandledError,
    UnhandledErrorDispatcher,
    UnhandledErrorDisplayList,
}

exports.models = {
    ApplicationInterfaceDefinition,
    ApplicationModule,
    BatchQueue,
    BatchQueueResourcePolicy,
    ComputeResourcePolicy,
    DataType,
    Experiment,
    ExperimentState,
    FullExperiment,
    Group,
    GroupComputeResourcePreference,
    GroupPermission,
    GroupResourceProfile,
    InputDataObjectType,
    OutputDataObjectType,
    Project,
    ResourcePermissionType,
    SharedEntity,
    SummaryType,
    UserPermission,
}

exports.services = {
    ApplicationDeploymentService,
    ApplicationInterfaceService,
    ApplicationModuleService,
    CredentialSummaryService: ServiceFactory.service("CredentialSummaries"),
    ExperimentService,
    ExperimentSearchService,
    FullExperimentService,
    ProjectService,
    GroupService,
    GroupResourceProfileService: ServiceFactory.service("GroupResourceProfiles"),
    UserProfileService,
    ComputeResourceService: ServiceFactory.service("ComputeResources"),
    CloudJobSubmissionService,
    GlobusJobSubmissionService,
    LocaJobSubmissionService,
    SshJobSubmissionService,
    UnicoreJobSubmissionService,
    GridFTPDataMovementService,
    SCPDataMovementService,
    UnicoreDataMovementService,
    ServiceFactory,
}

exports.utils = {
    FetchUtils,
    PaginationIterator,
}
