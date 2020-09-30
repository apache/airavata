import BaseModel from "./BaseModel";

const FIELDS = [
  "appModuleId",
  "appModuleName",
  "appModuleVersion",
  "appModuleDescription",
  "userHasWriteAccess",
];

export default class ApplicationModule extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
