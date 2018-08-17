
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
}

export default new NotificationList();
