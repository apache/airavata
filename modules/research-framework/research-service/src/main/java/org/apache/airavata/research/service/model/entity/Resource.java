package org.apache.airavata.research.service.model.entity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Resource {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String headerImage;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "resource_authors",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> authors = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "resource_tags",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrivacyEnum privacy;

    public abstract ResourceTypeEnum getType();

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.util.Set<User> getAuthors() {
        return authors;
    }

    public void setAuthors(java.util.Set<User> authors) {
        this.authors = authors;
    }

    public java.util.Set<Tag> getTags() {
        return tags;
    }

    public void setTags(java.util.Set<Tag> tags) {
        this.tags = tags;
    }

    public org.apache.airavata.research.service.enums.StatusEnum getStatus() {
        return status;
    }

    public void setStatus(org.apache.airavata.research.service.enums.StatusEnum status) {
        this.status = status;
    }

    public org.apache.airavata.research.service.enums.PrivacyEnum getPrivacy() {
        return privacy;
    }

    public void setPrivacy(org.apache.airavata.research.service.enums.PrivacyEnum privacy) {
        this.privacy = privacy;
    }
}
