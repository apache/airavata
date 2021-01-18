import BaseModel from "./BaseModel";

const FIELDS = [
  "replicaId",
  "productUri",
  "replicaName",
  "replicaDescription",
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "lastModifiedTime",
    type: "date",
  },
  {
    name: "validUntilTime",
    type: "date",
  },
  "replicaLocationCategory",
  "replicaPersistentType",
  "storageResourceId",
  "filePath",
  "replicaMetadata",
];

export default class DataReplicaLocation extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
