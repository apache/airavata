/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */


namespace java org.apache.airavata.model.dbevent
namespace php Airavata.Model.Dbevent
namespace py airavata.model.dbevent

// type of db-crud operation needed for replication
enum CrudType {
    CREATE,
    READ,
    UPDATE,
    DELETE
}

// type of db-entity being replicated
enum EntityType {
    USER_PROFILE,
    TENANT,
    GROUP,
    PROJECT,
    EXPERIMENT,
    APPLICATION,
    SHARING,
    REGISTRY
}

// type of db-replication event
enum DBEventType {
    PUBLISHER,
    SUBSCRIBER
}

// details pertaining to publish event-type
struct DBEventPublisherContext {
    1:  required CrudType crudType,         // type of crud operation
    2:  required EntityType entityType,     // type of db-entity replicated
    3:  required binary entityDataModel     // actual entity thrift-data-model
}

// context set by publisher
struct DBEventPublisher {
    1:  required DBEventPublisherContext publisherContext   // set by publisher (replication initiator)
}

// details pertaining to subscribe event-type
struct DBEventSubscriber {
    1:  required string subscriberService       // set by subscriber (replication requester)
}

// either variable set, depending on event-type
union DBEventMessageContext {
    1:  DBEventPublisher publisher,
    2:  DBEventSubscriber subscriber
}

// actual db-event message transmitted
struct DBEventMessage {
    1:  required DBEventType dbEventType,                   // event: publish/subscribe
    2:  required DBEventMessageContext messageContext,      // event details
    3:  required string publisherService                    // source of replication (publisher)
}

