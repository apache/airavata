import { MetadataType } from "./MetadataType";

export interface DatasetType {
  metadata: MetadataType;
  datasetUrl: string;
  private: true | false;
}
