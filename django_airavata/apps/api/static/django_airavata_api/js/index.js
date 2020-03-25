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
import ComputeResourceReservation from "./models/ComputeResourceReservation";
import DataProduct from "./models/DataProduct";
import DataType from "./models/DataType";
import Experiment from "./models/Experiment";
import ExperimentSearchFields from "./models/ExperimentSearchFields";
import ExperimentState from "./models/ExperimentState";
import FullExperiment from "./models/FullExperiment";
import Group from "./models/Group";
import GroupComputeResourcePreference from "./models/GroupComputeResourcePreference";
import GroupPermission from "./models/GroupPermission";
import GroupResourceProfile from "./models/GroupResourceProfile";
import IAMUserProfile from "./models/IAMUserProfile";
import InputDataObjectType from "./models/InputDataObjectType";
import JobState from "./models/JobState";
import Notification from "./models/Notification";
import NotificationPriority from "./models/NotificationPriority";
import OutputDataObjectType from "./models/OutputDataObjectType";
import ParallelismType from "./models/ParallelismType";
import Project from "./models/Project";
import ResourcePermissionType from "./models/ResourcePermissionType";
import SetEnvPaths from "./models/SetEnvPaths";
import SharedEntity from "./models/SharedEntity";
import StoragePreference from "./models/StoragePreference";
import SummaryType from "./models/SummaryType";
import UserPermission from "./models/UserPermission";

import CloudJobSubmissionService from "./services/CloudJobSubmissionService";
import GlobusJobSubmissionService from "./services/GlobusJobSubmissionService";
import LocaJobSubmissionService from "./services/LocaJobSubmissionService";
import SshJobSubmissionService from "./services/SshJobSubmissionService";
import UnicoreJobSubmissionService from "./services/UnicoreJobSubmissionService";
import SCPDataMovementService from "./services/SCPDataMovementService";
import GridFTPDataMovementService from "./services/GridFTPDataMovementService";
import UnicoreDataMovementService from "./services/UnicoreDataMovementService";
import ServiceFactory from "./services/ServiceFactory";

import Session from "./session/Session";

import FetchUtils from "./utils/FetchUtils";
import PaginationIterator from "./utils/PaginationIterator";
import StringUtils from "./utils/StringUtils";

const errors = {
  ErrorUtils,
  UnhandledError,
  UnhandledErrorDispatcher,
  UnhandledErrorDisplayList
};

const models = {
  ApplicationDeploymentDescription,
  ApplicationInterfaceDefinition,
  ApplicationModule,
  BaseModel,
  BatchQueue,
  BatchQueueResourcePolicy,
  CommandObject,
  ComputeResourcePolicy,
  ComputeResourceReservation,
  DataProduct,
  DataType,
  Experiment,
  ExperimentSearchFields,
  ExperimentState,
  FullExperiment,
  Group,
  GroupComputeResourcePreference,
  GroupPermission,
  GroupResourceProfile,
  IAMUserProfile,
  InputDataObjectType,
  JobState,
  Notification,
  NotificationPriority,
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

const services = {
  APIServerStatusCheckService: ServiceFactory.service("APIServerStatusCheck"),
  ApplicationDeploymentService: ServiceFactory.service(
    "ApplicationDeployments"
  ),
  ApplicationInterfaceService: ServiceFactory.service("ApplicationInterfaces"),
  ApplicationModuleService: ServiceFactory.service("ApplicationModules"),
  CloudJobSubmissionService,
  ComputeResourceService: ServiceFactory.service("ComputeResources"),
  CredentialSummaryService: ServiceFactory.service("CredentialSummaries"),
  DataProductService: ServiceFactory.service("DataProducts"),
  ExperimentSearchService: ServiceFactory.service("ExperimentSearch"),
  ExperimentService: ServiceFactory.service("Experiments"),
  ExperimentStatisticsService: ServiceFactory.service("ExperimentStatistics"),
  FullExperimentService: ServiceFactory.service("FullExperiments"),
  GatewayResourceProfileService: ServiceFactory.service(
    "GatewayResourceProfiles"
  ),
  GlobusJobSubmissionService,
  GridFTPDataMovementService,
  GroupResourceProfileService: ServiceFactory.service("GroupResourceProfiles"),
  GroupService: ServiceFactory.service("Groups"),
  LocaJobSubmissionService,
  LoggingService: ServiceFactory.service("LogRecords"),
  IAMUserProfileService: ServiceFactory.service("IAMUserProfiles"),
  ManageNotificationService: ServiceFactory.service("ManageNotifications"),

  ParserService: ServiceFactory.service("Parsers"),
  ProjectService: ServiceFactory.service("Projects"),
  SCPDataMovementService,
  ServiceFactory,
  SettingsService: ServiceFactory.service("Settings"),
  SharedEntityService: ServiceFactory.service("SharedEntities"),
  SshJobSubmissionService,
  StoragePreferenceService: ServiceFactory.service("StoragePreferences"),
  StorageResourceService: ServiceFactory.service("StorageResources"),
  UnicoreDataMovementService,
  UnicoreJobSubmissionService,
  UnverifiedEmailUserProfileService: ServiceFactory.service(
    "UnverifiedEmailUsers"
  ),
  UserProfileService: ServiceFactory.service("UserProfiles"),
  UserStoragePathService: ServiceFactory.service("UserStoragePaths"),
  WorkspacePreferencesService: ServiceFactory.service("WorkspacePreferences")
};

const session = {
  Session
};

const utils = {
  FetchUtils,
  PaginationIterator,
  StringUtils
};

export default {
  errors,
  models,
  services,
  session,
  utils
};

export { errors, models, services, session, utils };
