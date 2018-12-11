import ErrorUtils from "./errors/ErrorUtils";
import UnhandledError from "./errors/UnhandledError";
import UnhandledErrorDispatcher from "./errors/UnhandledErrorDispatcher";
import UnhandledErrorDisplayList from "./errors/UnhandledErrorDisplayList";

import ApplicationDeploymentDescription from "./models/ApplicationDeploymentDescription";
import ApplicationInterfaceDefinition from "./models/ApplicationInterfaceDefinition";
import ApplicationModule from "./models/ApplicationModule";
import BaseModel from "./models/BaseModel";
import BatchQueue from "./models/BatchQueue";
import BatchQueueResourcePolicy from "./models/BatchQueueResourcePolicy";
import CommandObject from "./models/CommandObject";
import ComputeResourcePolicy from "./models/ComputeResourcePolicy";
import DataType from "./models/DataType";
import Experiment from "./models/Experiment";
import ExperimentState from "./models/ExperimentState";
import FullExperiment from "./models/FullExperiment";
import Group from "./models/Group";
import GroupComputeResourcePreference from "./models/GroupComputeResourcePreference";
import GroupPermission from "./models/GroupPermission";
import GroupResourceProfile from "./models/GroupResourceProfile";
import InputDataObjectType from "./models/InputDataObjectType";
import OutputDataObjectType from "./models/OutputDataObjectType";
import ParallelismType from "./models/ParallelismType";
import Project from "./models/Project";
import ResourcePermissionType from "./models/ResourcePermissionType";
import SetEnvPaths from "./models/SetEnvPaths";
import SharedEntity from "./models/SharedEntity";
import StoragePreference from "./models/StoragePreference";
import SummaryType from "./models/SummaryType";
import UserPermission from "./models/UserPermission";

import ExperimentService from "./services/ExperimentService";
import ExperimentSearchService from "./services/ExperimentSearchService";
import FullExperimentService from "./services/FullExperimentService";
import ProjectService from "./services/ProjectService";
import UserProfileService from "./services/UserProfileService";
import CloudJobSubmissionService from "./services/CloudJobSubmissionService";
import GlobusJobSubmissionService from "./services/GlobusJobSubmissionService";
import LocaJobSubmissionService from "./services/LocaJobSubmissionService";
import SshJobSubmissionService from "./services/SshJobSubmissionService";
import UnicoreJobSubmissionService from "./services/UnicoreJobSubmissionService";
import SCPDataMovementService from "./services/SCPDataMovementService";
import GridFTPDataMovementService from "./services/GridFTPDataMovementService";
import UnicoreDataMovementService from "./services/UnicoreDataMovementService";
import ServiceFactory from "./services/ServiceFactory";

import FetchUtils from "./utils/FetchUtils";
import PaginationIterator from "./utils/PaginationIterator";
import StringUtils from "./utils/StringUtils";

exports.errors = {
  ErrorUtils,
  UnhandledError,
  UnhandledErrorDispatcher,
  UnhandledErrorDisplayList
};

exports.models = {
  ApplicationDeploymentDescription,
  ApplicationInterfaceDefinition,
  ApplicationModule,
  BaseModel,
  BatchQueue,
  BatchQueueResourcePolicy,
  CommandObject,
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
  ParallelismType,
  Project,
  ResourcePermissionType,
  SetEnvPaths,
  SharedEntity,
  StoragePreference,
  SummaryType,
  UserPermission
};

exports.services = {
  ApplicationDeploymentService: ServiceFactory.service(
    "ApplicationDeployments"
  ),
  ApplicationInterfaceService: ServiceFactory.service("ApplicationInterfaces"),
  ApplicationModuleService: ServiceFactory.service("ApplicationModules"),
  CloudJobSubmissionService,
  ComputeResourceService: ServiceFactory.service("ComputeResources"),
  CredentialSummaryService: ServiceFactory.service("CredentialSummaries"),
  ExperimentSearchService: ServiceFactory.service("ExperimentSearch"),
  ExperimentService: ServiceFactory.service("Experiments"),
  FullExperimentService,
  GatewayResourceProfileService: ServiceFactory.service(
    "GatewayResourceProfiles"
  ),
  GlobusJobSubmissionService,
  GridFTPDataMovementService,
  GroupResourceProfileService: ServiceFactory.service("GroupResourceProfiles"),
  GroupService: ServiceFactory.service("Groups"),
  LocaJobSubmissionService,
  ParserService: ServiceFactory.service("Parsers"),
  ProjectService,
  SCPDataMovementService,
  ServiceFactory,
  SharedEntityService: ServiceFactory.service("SharedEntities"),
  SshJobSubmissionService,
  StoragePreferenceService: ServiceFactory.service("StoragePreferences"),
  StorageResourceService: ServiceFactory.service("StorageResources"),
  UnicoreDataMovementService,
  UnicoreJobSubmissionService,
  UserProfileService
};

exports.utils = {
  FetchUtils,
  PaginationIterator,
  StringUtils
};
