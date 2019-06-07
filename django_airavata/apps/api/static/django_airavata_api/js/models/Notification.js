import BaseModel from "./BaseModel";
import NotificationPriority from "./NotificationPriority";

const FIELDS = [
  "notificationId",
  "gatewayId",
  "title",
  "notificationMessage",
  "creationTime",
  "publishedTime",
  "expirationTime",
  {
      name: "priority",
      type: NotificationPriority
  }
];

export default class Notification extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
