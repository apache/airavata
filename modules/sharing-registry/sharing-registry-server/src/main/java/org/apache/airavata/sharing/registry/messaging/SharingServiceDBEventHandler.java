package org.apache.airavata.sharing.registry.messaging;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.server.SharingRegistryServer;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.utils.ThriftDataModelConversion;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventHandler implements MessageHandler {

    private final static Logger log = LoggerFactory.getLogger(SharingServiceDBEventHandler.class);

    @Override
    public void onMessage(MessageContext messageContext) {

        log.info("New DB Event message to sharing service.");

        try{

            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());

            DBEventMessage dbEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(bytes, dbEventMessage);

            log.info("DB Event message to sharing service from " + dbEventMessage.getPublisherService());

            DBEventMessageContext dBEventMessageContext = dbEventMessage.getMessageContext();

            switch (dBEventMessageContext.getPublisher().getPublisherContext().getEntityType()){

                case USER_PROFILE :

                    log.info("User profile specific DB Event communicated by " + dbEventMessage.getPublisherService());

                    UserProfile  userProfile = new UserProfile();
                    ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), userProfile);

                    SharingRegistryService.Client sharingRegistryClient = SharingRegistryServiceClientFactory.createSharingRegistryClient(ServerSettings.getSetting(SharingRegistryServer.SHARING_REG_SERVER_HOST), Integer.parseInt(ServerSettings.getSetting(SharingRegistryServer.SHARING_REG_SERVER_PORT)));
                    User user = ThriftDataModelConversion.getUser(userProfile);

                    switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()){

                        case CREATE:
                            log.info("Creating user. User Id : " + user.getUserId());
                            sharingRegistryClient.createUser(user);
                            break;
                        case READ:
                            //FIXME: Remove if not required
                            break;

                        case UPDATE:
                            log.info("Updating user. User Id : " + user.getUserId());
                            sharingRegistryClient.updatedUser(user);
                            break;
                        case DELETE:
                            log.info("Deleting user. User Id : " + user.getUserId());
                            sharingRegistryClient.deleteUser(user.getDomainId(), user.getUserId());
                            break;
                    }
                    break;

                default: log.error("Handler not defined for " + dBEventMessageContext.getPublisher().getPublisherContext().getEntityType());
            }

        } catch (TException e) {
            log.error("Error processing message.", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error fetching application settings.", e);
        }
    }
}
