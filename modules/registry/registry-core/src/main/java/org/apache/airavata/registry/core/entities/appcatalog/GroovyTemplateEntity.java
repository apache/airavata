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
 */
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;

@Entity
@Table(name = "GROOVY_TEMPLATE")
public class GroovyTemplateEntity {

    @Column(name = "TEMPLATE_ID")
    @Id
    private String templateId;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "TEMPLATE_BODY")
    @Lob
    private String templateBody;

    @Column(name = "READ_ONLY", columnDefinition="tinyint(1) default 1")
    private boolean readOnly;

    @Column(name = "RESOURCE_JOB_MANAGER_TYPE")
    private String resourceJobManagerType;

    public String getTemplateId() {
        return templateId;
    }

    public GroovyTemplateEntity setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public GroovyTemplateEntity setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getTemplateBody() {
        return templateBody;
    }

    public GroovyTemplateEntity setTemplateBody(String templateBody) {
        this.templateBody = templateBody;
        return this;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public GroovyTemplateEntity setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public String getResourceJobManagerType() {
        return resourceJobManagerType;
    }

    public GroovyTemplateEntity setResourceJobManagerType(String resourceJobManagerType) {
        this.resourceJobManagerType = resourceJobManagerType;
        return this;
    }
}
