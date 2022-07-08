import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "username",
  "first_name",
  "last_name",
  "email",
  "pending_email_change",
  "complete",
  "username_valid",
  "ext_user_profile_valid",
];

export default class User extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
