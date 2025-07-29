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
package org.apache.airavata.research.service.v2.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.service.v2.enums.ResourceTypeEnumV2;

@Entity
@Table(name = "CODE_V2")
public class Code extends ResourceV2 {

    @Column(nullable = false)
    @NotBlank(message = "Code type is required")
    @Size(max = 100, message = "Code type must not exceed 100 characters")
    private String codeType; // MODEL, NOTEBOOK, REPOSITORY, HYBRID

    // From ModelResource
    @Column
    private String applicationInterfaceId;

    @Column
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    // From NotebookResource
    @Column
    private String notebookPath;

    // From RepositoryResource
    @Column
    private String repositoryUrl;

    // Combined metadata fields
    @Column
    @Size(max = 100, message = "Programming language must not exceed 100 characters")
    private String programmingLanguage; // Python, R, Java, etc.

    @Column
    @Size(max = 100, message = "Framework must not exceed 100 characters")
    private String framework; // TensorFlow, PyTorch, Scikit-learn, etc.

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "code_dependencies", joinColumns = @JoinColumn(name = "code_id"))
    @Column(name = "dependency")
    private Set<String> dependencies = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Override
    public ResourceTypeEnumV2 getType() {
        return ResourceTypeEnumV2.CODE;
    }

    // Default constructor
    public Code() {}

    // Main constructor for creating code entities
    public Code(String name, String description, String codeType, String programmingLanguage, 
                String framework, Set<String> authors, Set<TagV2> tags) {
        this.setName(name);
        this.setDescription(description);
        this.codeType = codeType;
        this.programmingLanguage = programmingLanguage;
        this.framework = framework;
        this.setAuthors(authors != null ? authors : new HashSet<>());
        this.setTags(tags != null ? tags : new HashSet<>());
        this.setHeaderImage(""); // Default empty header image
    }

    // Getters and Setters for Code-specific fields
    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNotebookPath() {
        return notebookPath;
    }

    public void setNotebookPath(String notebookPath) {
        this.notebookPath = notebookPath;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}