import BaseEnum from './BaseEnum'

export default class DataType extends BaseEnum {
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
