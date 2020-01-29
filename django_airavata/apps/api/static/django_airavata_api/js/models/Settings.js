import BaseModel from "./BaseModel";

const FIELDS = ["fileUploadMaxFileSize", "tusEndpoint", "pgaUrl"];

export default class Settings extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
