import BaseModel from "./BaseModel";
import UserStorageDirectory from "./UserStorageDirectory";
import UserStorageFile from "./UserStorageFile";

const FIELDS = [
  {
    name: "files",
    type: UserStorageFile,
    list: true,
  },
  {
    name: "directories",
    type: UserStorageDirectory,
    list: true,
  },
  {
    name: "parts",
    type: "string",
    list: true,
  },
  {
    name: "isDir",
    type: "boolean",
    list: false,
  },
];

export default class UserStoragePath extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
