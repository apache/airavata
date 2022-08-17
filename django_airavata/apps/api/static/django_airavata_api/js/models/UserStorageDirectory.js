import BaseModel from "./BaseModel";

const FIELDS = [
  "name",
  "path",
  { name: "createdTime", type: "date" },
  { name: "modifiedTime", type: "date" },
  "size",
  "hidden",
];

export default class UserStorageDirectory extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
