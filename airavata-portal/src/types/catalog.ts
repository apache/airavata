/**
 * Research Catalog Types
 *
 * Artifact Scope Model:
 * - USER: Artifacts owned by a specific user (stored in DB)
 * - GATEWAY: Artifacts owned at gateway level (stored in DB)
 * - DELEGATED: Artifacts accessible via group credentials but not directly owned (inferred, not stored)
 *
 * Only USER and GATEWAY can be set when creating artifacts.
 * DELEGATED is automatically inferred when returning artifacts accessible via groups.
 */

export enum ArtifactType {
  DATASET = "DATASET",
  REPOSITORY = "REPOSITORY",
}

export enum ArtifactStatus {
  NONE = "NONE",
  PENDING = "PENDING",
  VERIFIED = "VERIFIED",
  REJECTED = "REJECTED",
}

export enum Privacy {
  PUBLIC = "PUBLIC",
  PRIVATE = "PRIVATE",
}

export interface Tag {
  id: string;
  name: string;
  color?: string;
}

/**
 * Artifact scope enum.
 *
 * - USER: Artifact owned by a specific user (stored in DB)
 * - GATEWAY: Artifact owned at gateway level (stored in DB)
 * - DELEGATED: Artifact accessible via group credentials but not directly owned (inferred at runtime, not stored)
 *
 * Only USER and GATEWAY can be set when creating artifacts.
 * DELEGATED is automatically inferred by the backend when returning artifacts.
 */
export enum ArtifactScope {
  USER = "USER",
  GATEWAY = "GATEWAY",
  DELEGATED = "DELEGATED",
}

export interface Artifact {
  id: string;
  name: string;
  description: string;
  type: ArtifactType;
  status: ArtifactStatus;
  privacy: Privacy;
  scope?: ArtifactScope;
  authors: string[];
  tags: Tag[];
  headerImage?: string;
  createdAt: number;
  updatedAt?: number;
  ownerId?: string;
  groupResourceProfileId?: string;
}

export interface DatasetArtifact extends Artifact {
  type: ArtifactType.DATASET;
  datasetUrl: string;
  size?: number;
  format?: string;
}

export interface RepositoryArtifact extends Artifact {
  type: ArtifactType.REPOSITORY;
  repositoryUrl?: string;
  branch?: string;
  commit?: string;
  notebookPath?: string;
  jupyterServerUrl?: string;
  modelUrl?: string;
  applicationInterfaceId?: string;
  framework?: string;
}

export type CatalogArtifact = DatasetArtifact | RepositoryArtifact;

export interface ArtifactFilters {
  type?: ArtifactType;
  tags?: string[];
  nameSearch?: string;
  pageNumber?: number;
  pageSize?: number;
}
