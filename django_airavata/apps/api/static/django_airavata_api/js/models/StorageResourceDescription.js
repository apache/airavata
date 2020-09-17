import BaseModel from "./BaseModel";

const FIELDS = [
  "storageResourceId",
  "hostName",
  "storageResourceDescription",
  "enabled",
  "dataMovementInterfaces",
  "creationTime",
  "updateTime",
];

export default class StorageResourceDescription extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
