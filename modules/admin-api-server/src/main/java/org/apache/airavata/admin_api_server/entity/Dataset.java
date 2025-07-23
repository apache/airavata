package org.apache.airavata.admin_api_server.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "datasets")
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "dataset_tags", joinColumns = @JoinColumn(name = "dataset_id"))
    @Column(name = "tag")
    private List<String> tags;

    @ElementCollection
    @CollectionTable(name = "dataset_authors", joinColumns = @JoinColumn(name = "dataset_id"))
    @Column(name = "author")
    private List<String> authors;

    @Column(name = "star_count")
    private Integer starCount = 0;

    @Column(name = "category")
    private String category;

    // Constructors
    public Dataset() {}

    public Dataset(String title, String description, List<String> tags, List<String> authors, String category) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.authors = authors;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public Integer getStarCount() { return starCount; }
    public void setStarCount(Integer starCount) { this.starCount = starCount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}