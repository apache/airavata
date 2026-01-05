/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

-- Research catalog schema (research-service)

CREATE TABLE RESOURCE (
    id VARCHAR(48) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    headerImage VARCHAR(255) NOT NULL,
    status VARCHAR(64) NOT NULL,
    state VARCHAR(64) NOT NULL,
    privacy VARCHAR(64) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Joined inheritance subtype tables (JOINED strategy)

CREATE TABLE REPOSITORY_RESOURCE (
    id VARCHAR(48) NOT NULL,
    repositoryUrl VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);

CREATE TABLE DATASET_RESOURCE (
    id VARCHAR(48) NOT NULL,
    datasetUrl VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);

CREATE TABLE NOTEBOOK_RESOURCE (
    id VARCHAR(48) NOT NULL,
    notebookPath VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);

CREATE TABLE MODEL_RESOURCE (
    id VARCHAR(48) NOT NULL,
    applicationInterfaceId VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);

CREATE TABLE TAG (
    id VARCHAR(48) NOT NULL,
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uniq_tag_value (value)
);

-- ElementCollection authors for Resource
CREATE TABLE resource_authors (
    resource_id VARCHAR(48) NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (resource_id, author_id),
    FOREIGN KEY (resource_id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);

-- ManyToMany tags for Resource
CREATE TABLE resource_tags (
    resource_id VARCHAR(48) NOT NULL,
    tag_id VARCHAR(48) NOT NULL,
    PRIMARY KEY (resource_id, tag_id),
    FOREIGN KEY (resource_id) REFERENCES RESOURCE(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES TAG(id) ON DELETE CASCADE
);

CREATE TABLE PROJECT (
    id VARCHAR(48) NOT NULL,
    name VARCHAR(255) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    repository_resource_id VARCHAR(48) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    state VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (repository_resource_id) REFERENCES REPOSITORY_RESOURCE(id)
);

-- ManyToMany datasets for Project
CREATE TABLE project_dataset (
    project_id VARCHAR(48) NOT NULL,
    dataset_resource_id VARCHAR(48) NOT NULL,
    PRIMARY KEY (project_id, dataset_resource_id),
    FOREIGN KEY (project_id) REFERENCES PROJECT(id) ON DELETE CASCADE,
    FOREIGN KEY (dataset_resource_id) REFERENCES DATASET_RESOURCE(id) ON DELETE CASCADE
);

CREATE TABLE SESSION (
    id VARCHAR(48) NOT NULL,
    sessionName VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    project_id VARCHAR(48) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (project_id) REFERENCES PROJECT(id) ON DELETE CASCADE
);

CREATE TABLE RESOURCE_STAR (
    id VARCHAR(48) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    resource_id VARCHAR(48) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_resource_star_user (user_id),
    INDEX idx_resource_star_resource (resource_id),
    FOREIGN KEY (resource_id) REFERENCES RESOURCE(id) ON DELETE CASCADE
);


