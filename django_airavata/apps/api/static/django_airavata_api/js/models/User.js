import BaseModel from "./BaseModel";

const FIELDS = ["id", "username", "first_name", "last_name", "email"];

export default class User extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
