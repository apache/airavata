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
package org.apache.airavata.models.sharing.registry;

/**
 * Container object for search criteria.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.SearchCriteria}.
 *
 * @param searchField Entity search field.
 * @param value Search value.
 * @param searchCondition EQUAL, LIKE etc.
 */
public record SearchCriteria(EntitySearchField searchField, String value, SearchCondition searchCondition) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private EntitySearchField searchField = EntitySearchField.ENTITY_SEARCH_FIELD_UNKNOWN;
        private String value = "";
        private SearchCondition searchCondition = SearchCondition.SEARCH_CONDITION_UNKNOWN;

        private Builder() {}

        private Builder(SearchCriteria source) {
            this.searchField = source.searchField;
            this.value = source.value;
            this.searchCondition = source.searchCondition;
        }

        public Builder setSearchField(EntitySearchField searchField) {
            this.searchField = searchField;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setSearchCondition(SearchCondition searchCondition) {
            this.searchCondition = searchCondition;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(searchField, value, searchCondition);
        }
    }
}
