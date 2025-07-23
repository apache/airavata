package org.apache.airavata.admin_api_server.dto;

import java.util.List;

/**
 * Data Transfer Object for Model entity
 * Used for API responses to control what data is exposed to clients
 */
public class ModelDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private List<String> authors;
    private Integer starCount;
    private String category;

    // Constructors
    public ModelDTO() {}

    public ModelDTO(Long id, String title, String description, List<String> tags, 
                   List<String> authors, Integer starCount, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.authors = authors;
        this.starCount = starCount;
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