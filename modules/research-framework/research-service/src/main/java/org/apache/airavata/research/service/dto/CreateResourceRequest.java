package org.apache.airavata.research.service.dto;

import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;

import java.util.Set;

public class CreateResourceRequest {
    public String name;
    public String description;
    Set<String> tags;
    public String headerImgage;
    Set<String> authors;
    PrivacyEnum privacy;

    public PrivacyEnum getPrivacy() {
        return privacy;
    }

    public void setPrivacy(PrivacyEnum privacy) {
        this.privacy = privacy;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
    }

    public String getHeaderImgage() {
        return headerImgage;
    }

    public void setHeaderImgage(String headerImgage) {
        this.headerImgage = headerImgage;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
