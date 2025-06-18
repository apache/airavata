import BaseEnum from "./BaseEnum";

export default class NotificationPriority extends BaseEnum {}
NotificationPriority.init(["LOW", "NORMAL", "HIGH"], true);
