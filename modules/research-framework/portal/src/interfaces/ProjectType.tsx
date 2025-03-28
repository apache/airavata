import { MetadataType } from "./MetadataType";

export interface ProjectType {
  metadata: MetadataType;
  notebookViewer?: string;
  repositoryUrl?: string;
}
