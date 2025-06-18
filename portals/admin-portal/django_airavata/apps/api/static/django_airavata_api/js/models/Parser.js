import BaseModel from "./BaseModel";
import ParserInputFile from "./ParserInput";
import ParserOutputFile from "./ParserOutput";

const FIELDS = [
  "id",
  "imageName",
  "outputDirPath",
  "inputDirPath",
  "executionCommand",
  {
    name: "inputFiles",
    list: true,
    type: ParserInputFile,
  },
  {
    name: "outputFiles",
    list: true,
    type: ParserOutputFile,
  },
  "gatewayId",
];

export default class Parser extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
