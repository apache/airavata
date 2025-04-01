import { DatasetResource, RepositoryResource } from "./ResourceType";

export interface ProjectType {
  id: string;
  name: string;
  repositoryResource: RepositoryResource;
  datasetResources: DatasetResource[];
  createdAt: string;
  updatedAt: string;
}
