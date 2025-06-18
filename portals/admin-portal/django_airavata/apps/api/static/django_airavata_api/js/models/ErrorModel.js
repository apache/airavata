import BaseModel from "./BaseModel";

const FIELDS = [
  "errorId",
  {
    name: "creationTime",
    type: "date",
  },
  "actualErrorMessage",
  "userFriendlyMessage",
  "transientOrPersistent",
  {
    name: "rootCauseErrorIdList",
    type: "string",
    list: true,
  },
];

export default class ErrorModel extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
