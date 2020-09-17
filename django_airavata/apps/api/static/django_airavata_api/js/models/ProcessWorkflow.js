import BaseModel from "./BaseModel";

const FIELDS = [
  "processId",
  "workflowId",
  {
    name: "creationTime",
    type: Date,
  },
  "type",
];

export default class ProcessWorkflow extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
