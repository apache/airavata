// Wire shapes of the Airavata API, mirroring the Go DTOs under api/*/dto.

export type Permission = "READ" | "WRITE";

// Who a record is shared with: a user, or every member of a group.
export type PrincipalType = "USER" | "GROUP";

// One grant from GET {resource}/{id}/shares (api/sharing/dto.ShareResponse).
export interface ResourceShare {
  resourceSharingId: string;
  resourceType: string;
  resourceId: string;
  principalType: PrincipalType;
  principalId: string;
  permission: Permission;
}

// api/iam/dto.GroupResponse.
export interface Group {
  groupId: string;
  groupName?: string | null;
  ownerId?: string | null;
  createdAt: number;
}

export type GroupRole = "ADMIN" | "MODERATOR" | "MEMBER";
export type GroupMemberStatus = "ACTIVE" | "INACTIVE";

// api/iam/dto.GroupMemberResponse.
export interface GroupMember {
  groupId: string;
  userId: string;
  groupRole: GroupRole;
  groupMemberStatus: GroupMemberStatus;
}

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

// Users

export type UserStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";
export type Role = "USER" | "ADMIN" | "SUPER_ADMIN";
export type AuthMethod = "CILOGON" | "SYSTEM" | "SERVICE_ACCOUNT";

// A user record as administrators see it in the user list, and users see themselves.
export interface UserAccount {
  userId: string;
  status: UserStatus | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  authMethod: AuthMethod | null;
  roles: Role[];
  createdAt: number;
}

// GET /api/v1/users/me: the caller, and what the server decided about them.
export interface CurrentUser {
  userId: string;
  status: UserStatus;
  authorities: string[];
  // What the caller's roles grant, such as "audit:read". Use it to hide what the caller
  // cannot do; the server enforces it regardless.
  permissions: string[];
  admin: boolean;
  user: UserAccount | null;
}

// GET /api/v1/permissions: every permission, for pages that let an administrator choose.
export interface PermissionInfo {
  permission: string; // "resource:action"
  description: string;
  // False for the permissions no service account may hold.
  grantableToServiceAccount: boolean;
}

// Service accounts (admin only)

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
  // What the account may do, sorted.
  permissions: string[];
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
  permissions: string[];
}

export interface ServiceAccountUpdate {
  displayName?: string | null;
  description?: string | null;
  email?: string | null;
  status?: UserStatus;
  // Replaces the account's whole set. Leave out to keep it as it is.
  permissions?: string[];
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
  qos?: string | null;
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

export type ProcessStatusType = "CREATED" | "SUBMITTED" | "RUNNING" | "COMPLETED" | "FAILED" | "STOPPED";

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

// How the dashboard opens a process's Jupyter Server: Airavata's proxy at baseUrl (a
// path under the API root), with token as the Jupyter token. linked says whether the
// job's Linkspan holds its link to Airavata right now.
export interface JupyterServer {
  processId: string;
  linked: boolean;
  baseUrl: string;
  token: string;
}

export interface ProcessStatus {
  processStatusId: string;
  processId: string | null;
  status: ProcessStatusType | null;
  log: string | null;
  timestamp: number | null;
}

export type ProcessWorkflowType = "BATCH_JOB_SUBMISSION" | "JOB_MONITORING" | "BATCH_JOB_COMPLETION";

export type ProcessWorkflowStatusType = "RUNNING" | "SUCCEEDED" | "FAILED" | "STOPPED";

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

export type TaskStatusType = "CREATED" | "EXECUTING" | "PAUSED" | "COMPLETED" | "FAILED" | "STOPPED";

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

// Audit log (admin only): GET /api/v1/audit-events
export type AuditAction = "CREATE" | "UPDATE" | "DELETE" | "UPSERT";

export interface AuditEvent {
  id: number;
  occurredAt: number;
  actor: string; // a user id, "root", "sa:<name>", or "system"
  action: AuditAction;
  entityType: string; // the table, e.g. "processes"
  entityId: string;
  changedFields: string; // comma-separated; empty unless an update named its columns
}

export interface AuditEventPage {
  items: AuditEvent[];
  nextCursor: number | null;
}
