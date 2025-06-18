import BaseEnum from "./BaseEnum";

export default class DataType extends BaseEnum {
  get isSimpleValueType() {
    return (
      [DataType.STRING, DataType.INTEGER, DataType.FLOAT].indexOf(this) >= 0
    );
  }
  get isFileValueType() {
    return (
      [
        DataType.URI,
        DataType.URI_COLLECTION,
        DataType.STDOUT,
        DataType.STDERR,
      ].indexOf(this) >= 0
    );
  }
}
DataType.init([
  "STRING",
  "INTEGER",
  "FLOAT",
  "URI",
  "URI_COLLECTION",
  "STDOUT",
  "STDERR",
]);
