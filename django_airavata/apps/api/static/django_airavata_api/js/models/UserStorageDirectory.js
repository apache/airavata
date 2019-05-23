import BaseModel from "./BaseModel";

const FIELDS = ["name", "path"];

export default class UserStorageDirectory extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
