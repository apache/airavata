import { errors } from 'django-airavata-api'

import Notification from './Notification'

let notificationIdSequence = 0;

class NotificationList {

    constructor() {
        this.notifications = [];
    }

    add(notification) {
        this.notifications.push(notification);
    }

    remove(notification) {
        const i = this.notifications.indexOf(notification);
        this.notifications.splice(i, 1);
    }

    get list() {
        return this.notifications;
    }

    getNextId() {
        return "NOTIFICATION-" + notificationIdSequence++;
    }
}

export default new NotificationList();
