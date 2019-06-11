import BaseModel from "./BaseModel";
import NotificationPriority from "./NotificationPriority";

const FIELDS = [
  "notificationId",
  "gatewayId",
  "title",
  "notificationMessage",
  {
    name:"creationTime",
    type: Date
  },
  {
    name:"publishedTime",
    type: Date
  },
  {
    name:"expirationTime",
    type: Date
  },
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
