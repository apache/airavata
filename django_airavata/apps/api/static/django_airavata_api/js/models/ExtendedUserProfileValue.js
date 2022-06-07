import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "value_type",
  "ext_user_profile_field",
  "text_value",
  "choices",
  "other_value",
  "agreement_value",
  "valid",
  "value_display",
];

export default class ExtendedUserProfileValue extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
