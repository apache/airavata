import BaseModel from "./BaseModel";

const FIELDS = [
  "name",
  "path",
  { name: "createdTime", type: "date" },
  { name: "modifiedTime", type: "date" },
  "size",
  "hidden",
  "userHasWriteAccess",
  "isSharedDir",
];

export default class UserStorageDirectory extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
