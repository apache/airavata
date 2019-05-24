import BaseModel from "./BaseModel";

const FIELDS = ["name", "path", { name: "createdTime", type: "date" }, "size"];

export default class UserStorageDirectory extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
