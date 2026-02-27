// Airavata API Types

// ─── Unified Resource Model ───

export interface Resource {
  resourceId: string;
  gatewayId: string;
  name: string;
  hostName: string;
  port: number;
  description?: string;
  capabilities: ResourceCapabilities;
  createdAt?: string;
  updatedAt?: string;
}

export interface ResourceCapabilities {
  compute?: ComputeCapability;
  storage?: StorageCapability;
}

export interface ComputeCapability {
  type: "SLURM" | "FORK";
  batchQueues?: BatchQueue[];
}

export interface StorageCapability {
  protocol: "SFTP" | "SCP";
  basePath?: string;
}

export interface BatchQueue {
  queueName: string;
  queueDescription?: string;
  maxRunTime?: number;
  maxNodes?: number;
  maxProcessors?: number;
  maxJobsInQueue?: number;
  maxMemory?: number;
  cpuPerNode?: number;
  gpuPerNode?: number;
  defaultNodeCount?: number;
  defaultCPUCount?: number;
  defaultWalltime?: number;
  queueSpecificMacros?: string;
  isDefaultQueue?: boolean;
}

// ─── Resource Binding (credential ↔ resource) ───

export interface ResourceBinding {
  bindingId: string;
  credentialId: string;
  resourceId: string;
  loginUsername: string;
  metadata?: Record<string, unknown>;
  enabled: boolean;
  gatewayId: string;
  createdAt?: string;
  updatedAt?: string;
}

// ─── Application (simplified) ───

export interface Application {
  applicationId: string;
  gatewayId: string;
  ownerName: string;
  name: string;
  version?: string;
  description?: string;
  inputs: AppField[];
  outputs: AppField[];
  installScript?: string;
  runScript?: string;
  scope: "GATEWAY" | "PRIVATE";
  createdAt?: string;
  updatedAt?: string;
}

export interface AppField {
  name: string;
  type: string;
  description?: string;
  required: boolean;
  defaultValue?: string;
}

// ─── Application Installation ───

export interface ApplicationInstallation {
  installationId: string;
  applicationId: string;
  resourceId: string;
  loginUsername: string;
  installPath?: string;
  status: "PENDING" | "INSTALLING" | "INSTALLED" | "FAILED";
  installedAt?: string;
  errorMessage?: string;
  createdAt?: string;
}

// ─── Allocation Project ───

export interface AllocationProject {
  allocationProjectId: string;
  projectCode: string;
  resourceId: string;
  description?: string;
  gatewayId: string;
  createdAt?: string;
}

// ─── Credential (new simplified) ───

export interface Credential {
  credentialId: string;
  gatewayId: string;
  userName: string;
  name: string;
  description?: string;
  credentialType: "SSH" | "PASSWORD";
  createdAt?: string;
}

// ─── Gateway ───

export interface Gateway {
  gatewayId: string;
  gatewayName: string;
  gatewayURL?: string;
  gatewayAdminFirstName?: string;
  gatewayAdminLastName?: string;
  gatewayAdminEmail?: string;
  domain?: string;
  emailAddress?: string;
  gatewayApprovalStatus?: string;
  gatewayAcronym?: string;
  gatewayPublicAbstract?: string;
  reviewProposalDescription?: string;
  declinedReason?: string;
  oauthClientId?: string;
  oauthClientSecret?: string;
  requestedCreationTime?: number;
  requesterUsername?: string;
}

// ─── Project ───

export interface Project {
  projectID: string;
  owner: string;
  gatewayId: string;
  name: string;
  description?: string;
  creationTime?: number;
  sharedUsers?: string[];
  sharedGroups?: string[];
}

// ─── Experiment ───

export interface ExperimentModel {
  experimentId: string;
  projectId: string;
  gatewayId: string;
  experimentType: ExperimentType;
  userName: string;
  experimentName: string;
  creationTime?: number;
  description?: string;
  executionId?: string;
  gatewayExecutionId?: string;
  gatewayInstanceId?: string;
  enableEmailNotification?: boolean;
  emailAddresses?: string[];
  userConfigurationData?: UserConfigurationDataModel;
  experimentInputs?: InputDataObjectType[];
  experimentOutputs?: OutputDataObjectType[];
  experimentStatus?: ExperimentStatus[];
  errors?: ErrorModel[];
  processes?: ProcessModel[];
  archiveStatus?: boolean;
}

export enum ExperimentType {
  SINGLE_APPLICATION = 'SINGLE_APPLICATION',
}

export interface UserConfigurationDataModel {
  airavataAutoSchedule?: boolean;
  overrideManualScheduledParams?: boolean;
  shareExperimentPublicly?: boolean;
  computationalResourceScheduling?: ComputationalResourceSchedulingModel;
  throttleResources?: boolean;
  userDN?: string;
  generateCert?: boolean;
  storageId?: string;
  experimentDataDir?: string;
  useUserCRPref?: boolean;
  groupResourceProfileId?: string;
}

export interface ComputationalResourceSchedulingModel {
  resourceHostId?: string;
  totalCPUCount?: number;
  nodeCount?: number;
  numberOfThreads?: number;
  queueName?: string;
  wallTimeLimit?: number;
  totalPhysicalMemory?: number;
  staticWorkingDir?: string;
  overrideLoginUserName?: string;
  overrideScratchLocation?: string;
  overrideAllocationProjectNumber?: string;
}

export interface ExperimentStatus {
  state: ExperimentState;
  timeOfStateChange?: number;
  reason?: string;
  statusId?: string;
}

export enum ExperimentState {
  CREATED = 'CREATED',
  VALIDATED = 'VALIDATED',
  SCHEDULED = 'SCHEDULED',
  LAUNCHED = 'LAUNCHED',
  EXECUTING = 'EXECUTING',
  CANCELING = 'CANCELING',
  CANCELED = 'CANCELED',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface ErrorModel {
  errorId: string;
  creationTime?: number;
  actualErrorMessage?: string;
  userFriendlyMessage?: string;
  transientOrPersistent?: boolean;
  rootCauseErrorIdList?: string[];
}

// ─── Input/Output Types ───

export interface InputDataObjectType {
  name: string;
  value?: string;
  type: DataType;
  applicationArgument?: string;
  standardInput?: boolean;
  userFriendlyDescription?: string;
  metaData?: string;
  inputOrder?: number;
  isRequired?: boolean;
  requiredToAddedToCommandLine?: boolean;
  dataStaged?: boolean;
  storageResourceId?: string;
  isReadOnly?: boolean;
  overrideFilename?: string;
}

export interface OutputDataObjectType {
  name: string;
  value?: string;
  type: DataType;
  applicationArgument?: string;
  isRequired?: boolean;
  requiredToAddedToCommandLine?: boolean;
  dataMovement?: boolean;
  location?: string;
  searchQuery?: string;
  outputStreaming?: boolean;
  storageResourceId?: string;
  metaData?: string;
}

export enum DataType {
  STRING = 'STRING',
  INTEGER = 'INTEGER',
  FLOAT = 'FLOAT',
  URI = 'URI',
  URI_COLLECTION = 'URI_COLLECTION',
  STDOUT = 'STDOUT',
  STDERR = 'STDERR',
  STDIN = 'STDIN',
}

/** Default system input (STDIN). Always present, non-editable. */
export const DEFAULT_SYSTEM_INPUT: InputDataObjectType = { name: "STDIN", type: DataType.STDIN, applicationArgument: "", isRequired: false };

/** Default system outputs (STDOUT, STDERR). Always present, non-editable. */
export const DEFAULT_SYSTEM_OUTPUTS: OutputDataObjectType[] = [
  { name: "STDOUT", type: DataType.STDOUT, applicationArgument: "", isRequired: false },
  { name: "STDERR", type: DataType.STDERR, applicationArgument: "", isRequired: false },
];

export function isSystemInputName(name: string): boolean {
  return name === "STDIN";
}

export function isSystemOutputName(name: string): boolean {
  return name === "STDOUT" || name === "STDERR";
}

// ─── Process ───

export interface ProcessModel {
  processId: string;
  experimentId: string;
  processType?: string;
  processMetadata?: Record<string, unknown>;
  creationTime?: number;
  lastUpdateTime?: number;
  processStatuses?: ProcessStatus[];
  processDetail?: string;
  applicationInterfaceId?: string;
  applicationDeploymentId?: string;
  computeResourceId?: string;
  processInputs?: InputDataObjectType[];
  processOutputs?: OutputDataObjectType[];
  processResourceSchedule?: ComputationalResourceSchedulingModel;
  tasks?: TaskModel[];
  taskDag?: string;
  processErrors?: ErrorModel[];
  gatewayExecutionId?: string;
  enableEmailNotification?: boolean;
  emailAddresses?: string[];
  storageResourceId?: string;
  userDn?: string;
  generateCert?: boolean;
  experimentDataDir?: string;
  userName?: string;
  useUserCRPref?: boolean;
  groupResourceProfileId?: string;
}

export interface ProcessStatus {
  state: ProcessState;
  timeOfStateChange?: number;
  reason?: string;
  statusId?: string;
}

export enum ProcessState {
  CREATED = 'CREATED',
  VALIDATED = 'VALIDATED',
  STARTED = 'STARTED',
  PRE_PROCESSING = 'PRE_PROCESSING',
  CONFIGURING_WORKSPACE = 'CONFIGURING_WORKSPACE',
  INPUT_DATA_STAGING = 'INPUT_DATA_STAGING',
  EXECUTING = 'EXECUTING',
  MONITORING = 'MONITORING',
  OUTPUT_DATA_STAGING = 'OUTPUT_DATA_STAGING',
  POST_PROCESSING = 'POST_PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLING = 'CANCELLING',
  CANCELED = 'CANCELED',
}

// ─── Task ───

export interface TaskModel {
  taskId: string;
  taskType: TaskTypes;
  parentProcessId: string;
  creationTime?: number;
  lastUpdateTime?: number;
  taskStatuses?: TaskStatus[];
  taskDetail?: string;
  taskErrors?: ErrorModel[];
  jobs?: JobModel[];
  maxRetry?: number;
  currentRetry?: number;
}

export enum TaskTypes {
  ENV_SETUP = 'ENV_SETUP',
  DATA_STAGING = 'DATA_STAGING',
  JOB_SUBMISSION = 'JOB_SUBMISSION',
  ENV_CLEANUP = 'ENV_CLEANUP',
  MONITORING = 'MONITORING',
  OUTPUT_FETCHING = 'OUTPUT_FETCHING',
}

export interface TaskStatus {
  state: TaskState;
  timeOfStateChange?: number;
  reason?: string;
  statusId?: string;
}

export enum TaskState {
  CREATED = 'CREATED',
  EXECUTING = 'EXECUTING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELED = 'CANCELED',
}

// ─── Job ───

export interface JobModel {
  jobId: string;
  taskId: string;
  processId: string;
  jobDescription: string;
  creationTime?: number;
  jobStatuses?: JobStatus[];
  computeResourceConsumed?: string;
  jobName?: string;
  workingDir?: string;
  stdOut?: string;
  stdErr?: string;
  exitCode?: number;
}

export interface JobStatus {
  jobState: JobState;
  timeOfStateChange?: number;
  reason?: string;
  statusId?: string;
}

export enum JobState {
  SUBMITTED = 'SUBMITTED',
  QUEUED = 'QUEUED',
  ACTIVE = 'ACTIVE',
  COMPLETE = 'COMPLETE',
  CANCELED = 'CANCELED',
  FAILED = 'FAILED',
  SUSPENDED = 'SUSPENDED',
  UNKNOWN = 'UNKNOWN',
}

// ─── Artifact ───
// An Artifact is a research artifact (dataset, repository, etc.) that can be
// used as input or generated as output of an experiment.

export interface ArtifactTag {
  id?: string;
  name?: string;
  color?: string;
}

export interface ArtifactModel {
  artifactUri: string;
  gatewayId: string;
  parentArtifactUri?: string;
  name: string;
  description?: string;
  ownerName: string;
  artifactType: ArtifactType;
  size?: number;
  creationTime?: number;
  lastModifiedTime?: number;
  metadata?: Record<string, string>;
  replicaLocations?: ArtifactReplicaModel[];
  childArtifacts?: ArtifactModel[];
  primaryStorageResourceId?: string;
  primaryFilePath?: string;
  status?: string;
  privacy?: string;
  scope?: string;
  ownerId?: string;
  headerImage?: string;
  format?: string;
  updatedAt?: number;
  authors?: string[];
  tags?: ArtifactTag[];
}

export enum ArtifactType {
  DATASET = 'DATASET',
  REPOSITORY = 'REPOSITORY',
}

export enum ArtifactStatus {
  NONE = 'NONE',
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
}

export enum ArtifactPrivacy {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE',
}

export enum ArtifactScope {
  USER = 'USER',
  GATEWAY = 'GATEWAY',
  DELEGATED = 'DELEGATED',
}

export interface ArtifactReplicaModel {
  replicaId: string;
  artifactUri: string;
  replicaName?: string;
  replicaDescription?: string;
  creationTime?: number;
  lastModifiedTime?: number;
  validUntilTime?: number;
  replicaLocationCategory: ReplicaLocationCategory;
  replicaPersistentType: ReplicaPersistentType;
  storageResourceId?: string;
  filePath?: string;
  replicaMetadata?: Record<string, string>;
}

export enum ReplicaLocationCategory {
  GATEWAY_DATA_STORE = 'GATEWAY_DATA_STORE',
  COMPUTE_RESOURCE = 'COMPUTE_RESOURCE',
  LONG_TERM_STORAGE_RESOURCE = 'LONG_TERM_STORAGE_RESOURCE',
}

export enum ReplicaPersistentType {
  TRANSIENT = 'TRANSIENT',
  PERSISTENT = 'PERSISTENT',
}

// ─── Workflow ───

export interface Workflow {
  workflowId: string;
  projectId: string;
  gatewayId: string;
  userName: string;
  workflowName: string;
  description?: string;
  steps: WorkflowStep[];
  edges: WorkflowEdge[];
  creationTime?: number;
  updateTime?: number;
}

export interface WorkflowStep {
  stepId: string;
  applicationId: string;
  label: string;
  inputs?: InputDataObjectType[];
  x: number;
  y: number;
}

export interface WorkflowEdge {
  fromStepId: string;
  toStepId: string;
  mappings: WorkflowEdgeMapping[];
}

export interface WorkflowEdgeMapping {
  fromOutput: string;
  toInput: string;
}

export interface WorkflowRun {
  runId: string;
  workflowId: string;
  userName: string;
  status: string;
  stepStates: Record<string, WorkflowRunStepState>;
  creationTime?: number;
  updateTime?: number;
}

export interface WorkflowRunStepState {
  experimentId?: string;
  status: string;
}

// ─── User ───

export interface User {
  id: string;
  name: string;
  email: string;
  gatewayId: string;
  roles?: string[];
}

// ─── Credential (legacy shape used by credentialsApi) ───

export interface CredentialSummary {
  token: string;
  gatewayId: string;
  username?: string;
  description?: string;
  publicKey?: string;
  persistedTime?: number;
  type: CredentialType;
}

export enum CredentialType {
  SSH = 'SSH',
  PASSWORD = 'PASSWORD',
  CERTIFICATE = 'CERTIFICATE',
}

export interface SSHCredential {
  token?: string;
  gatewayId: string;
  passphrase?: string;
  publicKey?: string;
  privateKey?: string;
  description?: string;
  persistedTime?: number;
}

export interface PasswordCredential {
  token?: string;
  gatewayId: string;
  portalUserName?: string;
  password: string;
  description?: string;
  persistedTime?: number;
}

// ─── Gateway Config ───

export interface GatewayConfigRequest {
  key: string;
  value: string;
}

export interface MaintenanceModeResponse {
  maintenanceMode: boolean;
  message?: string;
}
