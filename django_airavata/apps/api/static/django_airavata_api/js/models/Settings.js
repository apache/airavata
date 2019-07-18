import BaseModel from "./BaseModel";

const FIELDS = ["fileUploadMaxFileSize"];

export default class Settings extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
