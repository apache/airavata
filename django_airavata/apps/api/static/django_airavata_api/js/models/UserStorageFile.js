import BaseModel from "./BaseModel";

const FIELDS = [
  "name",
  "downloadURL",
  "dataProductURI",
  { name: "createdTime", type: "date" },
  { name: "modifiedTime", type: "date" },
  "size",
  "mimeType",
];

export default class UserStorageFile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
