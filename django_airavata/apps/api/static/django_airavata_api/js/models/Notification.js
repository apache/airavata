import BaseModel from "./BaseModel";
import NotificationPriority from "./NotificationPriority";

const FIELDS = [
  "notificationId",
  "gatewayId",
  "title",
  "notificationMessage",
  {
    name: "creationTime",
    type: Date,
  },
  {
    name: "publishedTime",
    type: Date,
  },
  {
    name: "expirationTime",
    type: Date,
  },
  {
    name: "priority",
    type: NotificationPriority,
  },
  "userHasWriteAccess",
  {
    name: "showInDashboard",
    type: "boolean",
    default: false,
  },
];

export default class Notification extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate() {
    let validationResults = {};
    if (this.isEmpty(this.title)) {
      validationResults["title"] = "Please provide a Title for this notice.";
    }
    if (
      this.isEmpty(this.notificationMessage) ||
      this.notificationMessage.length < 10
    ) {
      validationResults["notificationMessage"] =
        "Please provide the message with minimum 10 characters.";
    }
    if (this.isEmpty(this.publishedTime)) {
      validationResults["publishedTime"] = "Please select the publish time";
    }
    if (this.isEmpty(this.expirationTime)) {
      validationResults["expirationTime"] = "Please select the expiration time";
    }
    if (this.isEmpty(this.priority)) {
      validationResults["priority"] = "Please select the priority";
    }
    return validationResults;
  }
}
