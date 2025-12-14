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
package org.apache.airavata.common.model;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Domain model: DBEventPublisherContext
 */
public class DBEventPublisherContext {
    private CrudType crudType;
    private EntityType entityType;
    private ByteBuffer entityDataModel;

    public DBEventPublisherContext() {}

    public CrudType getCrudType() {
        return crudType;
    }

    public void setCrudType(CrudType crudType) {
        this.crudType = crudType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public ByteBuffer getEntityDataModel() {
        return entityDataModel;
    }

    public void setEntityDataModel(ByteBuffer entityDataModel) {
        this.entityDataModel = entityDataModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBEventPublisherContext that = (DBEventPublisherContext) o;
        return Objects.equals(crudType, that.crudType)
                && Objects.equals(entityType, that.entityType)
                && Objects.equals(entityDataModel, that.entityDataModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crudType, entityType, entityDataModel);
    }

    @Override
    public String toString() {
        return "DBEventPublisherContext{" + "crudType=" + crudType + ", entityType=" + entityType + ", entityDataModel="
                + entityDataModel + "}";
    }
}
