// Wire shapes of the Airavata API, mirroring the Go DTOs under api/*/dto.

export type Permission = "READ" | "WRITE";

export interface FieldError {
  field: string;
  message: string;
}

export interface ErrorEnvelope {
  status: number;
  error: string;
  message: string;
  fieldErrors?: FieldError[];
}

// Service accounts (admin only)

export type UserStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

export interface ServiceAccount {
  serviceAccountId: string; // "sa:<name>"
  name: string;
  displayName: string | null;
  description: string | null;
  email: string | null;
  status: UserStatus | null;
  createdBy: string;
  createdAt: number;
  tokenIssuedAt: number;
  tokenExpiresAt: number;
}

// Returned on create and regenerate: the only times the token is ever sent.
export interface ServiceAccountToken {
  serviceAccount: ServiceAccount;
  token: string;
}

export interface ServiceAccountCreate {
  name: string;
  displayName: string | null;
  description: string | null;
  email: string | null;
  validityDays: number | null;
}

export interface ServiceAccountUpdate {
  displayName?: string | null;
  description?: string | null;
  email?: string | null;
  status?: UserStatus;
}

// Credentials

export interface SSHCredential {
  sshCredentialId: string;
  sshCredentialName: string;
  publicKey: string;
  fullInteractive: boolean;
  ownerId: string;
}

export interface SSHCredentialRequest {
  sshCredentialName: string;
  publicKey: string;
  privateKey: string | null;
  passphrase: string | null;
  fullInteractive: boolean;
}

export interface InteractivePrompt {
  text: string;
  echo: boolean;
}

export interface InteractiveAuth {
  authId: string;
  hostName: string;
  port: number;
  loginUser: string;
  sshCredentialId: string;
  banner: string;
  name: string;
  instruction: string;
  prompts: InteractivePrompt[];
  createdAt: string;
}

export interface InteractiveAuthMessage {
  type: "challenge" | "accepted" | "error" | "idle";
  auth?: InteractiveAuth;
  authId?: string;
  message?: string;
}

// Compute

export interface ClusterPartition {
  partitionId?: string;
  clusterId?: string | null;
  name: string;
  description?: string | null;
  maxRunTime?: number | null;
  maxNodes?: number | null;
  maxProcessors?: number | null;
  maxJobsInQueue?: number | null;
  maxMemory?: number | null;
  cpuPerNode?: number | null;
  defaultNodeCount?: number | null;
  defaultCpuCount?: number | null;
  defaultWalltime?: number | null;
  gres?: string | null;
  nodes?: string | null;
  isDefaultQueue?: boolean | null;
  isCheckpointable?: boolean | null;
}

export interface SlurmCluster {
  slurmClusterId: string;
  clusterName: string;
  clusterDescription: string | null;
  headnodeHost: string;
  headnodePort: number;
  slurmHome: string | null;
  dataHost: string | null;
  dataPort: number | null;
  partitions: ClusterPartition[];
}

export interface SlurmClusterConfig {
  slurmClusterConfigId: string;
  name: string | null;
  description: string | null;
  ownerId: string | null;
  slurmClusterId: string;
  slurmCluster: SlurmCluster | null;
  loginUser: string;
  workRoot: string;
  sshCredentialId: string | null;
  sshCredential: SSHCredential | null;
  permission?: Permission;
}

export interface Allocation {
  account: string;
  description: string | null;
  // Partitions the account is limited to; empty means any.
  partitions: string[];
  qos: string[];
}

// Applications

export const INPUT_TYPES = ["STRING", "INTEGER", "FLOAT", "BOOLEAN", "FILE", "FILE_LIST", "DIRECTORY"] as const;
export const OUTPUT_TYPES = ["FILE", "FILE_LIST", "DIRECTORY"] as const;
export type InputType = (typeof INPUT_TYPES)[number];
export type OutputType = (typeof OUTPUT_TYPES)[number];

export const isFileType = (t: InputType | OutputType | null | undefined) =>
  t === "FILE" || t === "FILE_LIST" || t === "DIRECTORY";

export interface TemplateInput {
  inputId?: string;
  inputName: string;
  displayName: string | null;
  inputDescription: string | null;
  inputType: InputType | null;
  required: boolean;
  defaultValue: string | null;
}

export interface TemplateOutput {
  outputId?: string;
  outputName: string;
  displayName: string | null;
  outputDescription: string | null;
  outputType: OutputType | null;
}

export interface Template {
  templateId: string;
  templateName: string | null;
  templateDescription: string | null;
  inputs: TemplateInput[];
  outputs: TemplateOutput[];
}

export interface BatchJobConfig {
  batchJobConfigId?: string;
  wallTimeMinutes: number | null;
  allocation: string;
  cpus?: number | null;
  mem?: string | null;
  memPerCpu?: string | null;
  ntasksPerNode?: number | null;
  cpusPerTask?: number | null;
  nodes?: number | null;
  ntasks?: number | null;
  gres?: string | null;
  gpus?: number | null;
  memPerGpu?: string | null;
  cpusPerGpu?: string | null;
  gpusPerNode?: number | null;
  constraints?: string | null;
}

export interface BatchDeployment {
  deploymentId: string;
  templateId: string | null;
  slurmClusterId: string | null;
  slurmRunSection: string;
  defaultBatchJobConfig: BatchJobConfig | null;
  defaultPartition: string | null;
}

// Data

export interface SCPDataStorage {
  dataId: string;
  dataName: string | null;
  ownerId: string | null;
  hostName: string | null;
  port: number | null;
  loginUser: string | null;
  sshCredentialId: string | null;
  sshCredential: SSHCredential | null;
  permission?: Permission;
}

export type ProvisionStatus =
  | "REGISTERD"
  | "PROVISIONING"
  | "PROVISIONED"
  | "DEPROVISIONING"
  | "DEPROVISIONED"
  | "FAILED";

export interface DataProduct {
  dataId: string;
  dataName: string | null;
  dataDescription: string | null;
  isFile: boolean;
  path: string | null;
  provisionStatus: ProvisionStatus | null;
  ownerId: string | null;
  dataStorageId: string | null;
  dataStorageType: string;
  createdAt: number;
  permission?: Permission;
}

// Processes

export type ProcessStatusType = "CREATED" | "SUBMITTED" | "RUNNING" | "COMPLETED" | "FAILED";

export interface Mapping {
  templateInputMappingId?: string;
  templateOutputMappingId?: string;
  templateInputId?: string;
  templateOutputId?: string;
  value: string | null;
}

export interface BatchJobStatus {
  batchProcessStatusId: string;
  batchProcessId: string;
  status: string;
  updatedAt: number;
}

export interface BatchProcess {
  batchProcessId: string;
  deploymentId: string | null;
  jobId: string | null;
  batchJobConfig: BatchJobConfig | null;
  slurmClusterConfigId: string;
  baseWorkDir: string | null;
  inputMappings: Mapping[];
  outputMappings: Mapping[];
  batchJobStatuses: BatchJobStatus[];
  latestBatchJobStatus: BatchJobStatus | null;
}

export interface Process {
  processId: string;
  userId: string | null;
  processType: "BATCH_JOB" | "CLOUD_JOB" | null;
  lastStatusId: string | null;
  lastWorkflowId: string | null;
  batchProcess: BatchProcess | null;
}

export interface ProcessStatus {
  processStatusId: string;
  processId: string | null;
  status: ProcessStatusType | null;
  log: string | null;
  timestamp: number | null;
}

export type ProcessWorkflowType = "BATCH_JOB_SUBMISSION" | "JOB_MONITORING" | "BATCH_JOB_COMPLETION";

export type ProcessWorkflowStatusType = "RUNNING" | "SUCCEEDED" | "FAILED";

// One workflow run of a process, updated in place as it moves from RUNNING to its
// outcome. workflowId is the workflow engine's instance id.
export interface ProcessWorkflow {
  processWorkflowId: string;
  processId: string | null;
  workflowId: string;
  workflowType: ProcessWorkflowType | null;
  status: ProcessWorkflowStatusType | null;
  timestamp: number | null;
}

export type TaskType = "DATA_STAGING" | "JOB_SUBMISSION" | "JOB_MONITORING" | "INTERACTIVE_COMMAND";

export type TaskStatusType = "CREATED" | "EXECUTING" | "PAUSED" | "COMPLETED" | "FAILED" | "CANCELED";

export interface TaskStatus {
  taskStatusId: string;
  taskId: string;
  taskType: TaskType;
  processId: string | null;
  status: TaskStatusType | null;
  log: string | null;
  timestamp: number | null;
}

export interface Task {
  taskId: string;
  processId: string | null;
  onFailure: string | null;
  retryCount: number | null;
  taskOrder: number | null;
  // Kind-specific fields; rendered generically.
  [key: string]: unknown;
}
