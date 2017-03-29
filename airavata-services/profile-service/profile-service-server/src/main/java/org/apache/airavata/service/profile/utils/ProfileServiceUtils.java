package org.apache.airavata.service.profile.utils;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.dbevent.*;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goshenoy on 3/28/17.
 */
public class ProfileServiceUtils {

    private final static Logger logger = LoggerFactory.getLogger(ProfileServiceUtils.class);
    private static Publisher dbEventPublisher = null;

    /**
     * Returns singleton instance of dbEventPublisher
     * @return
     * @throws AiravataException
     */
    public static Publisher getDbEventPublisher() throws AiravataException {
        if (dbEventPublisher == null) {
            dbEventPublisher = MessagingFactory.getDBEventPublisher();
        }
        return dbEventPublisher;
    }

    /**
     * Constructs the dbEventMessageContext
     * @param entityType
     * @param crudType
     * @param entityModel
     * @return
     * @throws AiravataException
     */
    public static MessageContext getDBEventMessageContext(EntityType entityType, CrudType crudType, TBase entityModel) throws AiravataException {
        try {
            // set the publisherContext
            DBEventMessage dbEventMessage = new DBEventMessage();
            DBEventPublisherContext publisherContext = new DBEventPublisherContext();
            publisherContext.setCrudType(crudType);
            publisherContext.setEntityDataModel(ThriftUtils.serializeThriftObject(entityModel));
            publisherContext.setEntityType(entityType);

            // create dbEventPublisher with publisherContext
            DBEventPublisher dbEventPublisher = new DBEventPublisher();
            dbEventPublisher.setPublisherContext(publisherContext);

            // set messageContext to dbEventPublisher
            DBEventMessageContext dbMessageContext = DBEventMessageContext.publisher(dbEventPublisher);

            // set dbEventMessage with messageContext
            dbEventMessage.setDbEventType(DBEventType.PUBLISHER);
            dbEventMessage.setPublisherService(DBEventManagerConstants.getDbEventServiceName(entityType));
            dbEventMessage.setMessageContext(dbMessageContext);

            // construct and return messageContext
            return new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");
        } catch (Exception ex) {
            throw new AiravataException(ex.getMessage(), ex);
        }
    }
}
