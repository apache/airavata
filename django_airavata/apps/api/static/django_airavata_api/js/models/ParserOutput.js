import BaseModel from "./BaseModel";
import IOType from "./IOType";

const FIELDS = [
  "id",
  "name",
  "requiredOutput",
  "parserId",
  {
    name: "type",
    type: IOType,
  },
];

export default class ParserOutput extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
