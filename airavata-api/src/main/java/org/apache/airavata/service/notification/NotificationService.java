package org.apache.airavata.service.notification;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final RegistryServerHandler registryHandler;

    public NotificationService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String createNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return registryHandler.createNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while creating notification: " + e.getMessage(), e);
        }
    }

    public boolean updateNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return registryHandler.updateNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while updating notification: " + e.getMessage(), e);
        }
    }

    public boolean deleteNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return registryHandler.deleteNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting notification: " + e.getMessage(), e);
        }
    }

    public Notification getNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return registryHandler.getNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> getAllNotifications(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.getAllNotifications(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting all notifications: " + e.getMessage(), e);
        }
    }
}
