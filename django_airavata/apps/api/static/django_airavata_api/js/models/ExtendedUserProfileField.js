import BaseModel from "./BaseModel";
import ExtendedUserProfileFieldChoice from "./ExtendedUserProfileFieldChoice";
import ExtendedUserProfileFieldLink from "./ExtendedUserProfileFieldLink";

const FIELDS = [
  "id",
  "name",
  "help_text",
  "order",
  {
    name: "created_date",
    type: "date",
  },
  {
    name: "updated_date",
    type: "date",
  },
  "field_type",
  {
    name: "links",
    list: true,
    type: ExtendedUserProfileFieldLink,
  },
  // For user_agreement type
  "checkbox_label",
  // For single_choice and multi_choice types
  {
    name: "choices",
    list: true,
    type: ExtendedUserProfileFieldChoice,
  },
  "other",
];

export default class ExtendedUserProfileField extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
