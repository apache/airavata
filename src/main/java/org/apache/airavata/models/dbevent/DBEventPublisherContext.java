/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.models.dbevent;

import java.util.Arrays;
import java.util.Objects;

/**
 * Details pertaining to publish event-type.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.dbevent.proto.DBEventPublisherContext}.
 */
public record DBEventPublisherContext(CrudType crudType, EntityType entityType, byte[] entityDataModel) {

    public DBEventPublisherContext {
        entityDataModel = entityDataModel == null ? new byte[0] : entityDataModel.clone();
    }

    @Override
    public byte[] entityDataModel() {
        return entityDataModel.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBEventPublisherContext other)) {
            return false;
        }
        return crudType == other.crudType
                && entityType == other.entityType
                && Arrays.equals(entityDataModel, other.entityDataModel);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(crudType, entityType) + Arrays.hashCode(entityDataModel);
    }

    @Override
    public String toString() {
        return "DBEventPublisherContext[crudType=" + crudType + ", entityType=" + entityType
                + ", entityDataModel=byte[" + entityDataModel.length + "]]";
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private CrudType crudType = CrudType.CRUD_TYPE_UNKNOWN;
        private EntityType entityType = EntityType.ENTITY_TYPE_UNKNOWN;
        private byte[] entityDataModel = new byte[0];

        private Builder() {}

        private Builder(DBEventPublisherContext source) {
            this.crudType = source.crudType;
            this.entityType = source.entityType;
            this.entityDataModel = source.entityDataModel();
        }

        public Builder setCrudType(CrudType crudType) {
            this.crudType = crudType;
            return this;
        }

        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setEntityDataModel(byte[] entityDataModel) {
            this.entityDataModel = entityDataModel == null ? new byte[0] : entityDataModel.clone();
            return this;
        }

        public DBEventPublisherContext build() {
            return new DBEventPublisherContext(crudType, entityType, entityDataModel);
        }
    }
}
