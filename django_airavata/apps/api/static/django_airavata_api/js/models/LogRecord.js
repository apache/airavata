import BaseModel from "./BaseModel";

const FIELDS = [
  "level",
  "message",
  "details",
  {
    name: "stacktrace",
    type: "string",
    list: true,
  },
];

export default class LogRecord extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
