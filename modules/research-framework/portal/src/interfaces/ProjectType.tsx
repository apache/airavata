import {DatasetResource, RepositoryResource} from "./ResourceType";

export interface ProjectType {
  id: string;
  name: string;
  repositoryResource: RepositoryResource;
  datasetResources: DatasetResource[];
  createdAt: string;
  updatedAt: string;
  ownerId: string;
}

export interface ProjectPostData {
  name: string;
  repositoryId: string;
  datasetIds: string[];
  ownerId: string;
}
