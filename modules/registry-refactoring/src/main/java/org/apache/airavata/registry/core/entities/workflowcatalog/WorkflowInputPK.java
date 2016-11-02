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
package org.apache.airavata.registry.core.entities.workflowcatalog;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * The primary key class for the workflow_input database table.
 */
@Embeddable
public class WorkflowInputPK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    private String templateId;

    @Column(name = "INPUT_KEY")
    private String inputKey;

    public WorkflowInputPK() {
    }

    public String getTemplateId() {

        return this.templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public String getInputKey() {

        return this.inputKey;
    }

    public void setInputKey(String inputKey) {

        this.inputKey = inputKey;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkflowInputPK)) {
            return false;
        }
        WorkflowInputPK castOther = (WorkflowInputPK) other;
        return
                this.templateId.equals(castOther.templateId)
                        && this.inputKey.equals(castOther.inputKey);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.templateId.hashCode();
        hash = hash * prime + this.inputKey.hashCode();

        return hash;
    }
}