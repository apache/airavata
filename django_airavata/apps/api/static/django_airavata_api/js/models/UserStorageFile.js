import BaseModel from "./BaseModel";

const FIELDS = ["name", "downloadURL", "dataProductURI"];

export default class UserStorageFile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
