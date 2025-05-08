export interface CreateProjectRequest {
  name: string;
  ownerId: string;
  repositoryId: string;
  datasetIds: string[];
}
