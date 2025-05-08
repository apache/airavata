import { PrivacyEnum } from "./PrivacyEnum";
import { ResourceTypeEnum } from "./ResourceTypeEnum";
import { StatusEnum } from "./StatusEnum";
import { Tag } from "./TagType";
// import { User } from "./UserType";

export interface Resource {
  id: string; // UUID, immutable
  name: string;
  description: string;
  headerImage: string;
  authors: string[];
  tags: Tag[];
  status: StatusEnum;
  privacy: PrivacyEnum;
  type: ResourceTypeEnum;
}

export interface NotebookResource extends Resource {
  notebookPath: string;
}

export interface DatasetResource extends Resource {
  datasetUrl: string;
}

export interface RepositoryResource extends Resource {
  repositoryUrl: string;
}

export interface ModelResource extends Resource {
  applicationInterfaceId: string;
  version: string;
}