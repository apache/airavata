import BaseModel from "./BaseModel";
import ProcessStatus from "./ProcessStatus";
import DataProduct from "./DataProduct";

const FIELDS = [
  {
    name: "processStatus",
    type: ProcessStatus,
  },
  {
    name: "dataProducts",
    type: DataProduct,
    list: true,
  },
];

export default class IntermediateOutput extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
