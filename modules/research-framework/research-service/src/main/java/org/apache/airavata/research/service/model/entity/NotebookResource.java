package org.apache.airavata.research.service.model.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class NotebookResource extends Resource {
    @Column(nullable = false)
    private String notebookPath;

    public String getNotebookPath() {
        return notebookPath;
    }

    public void setNotebookPath(String notebookPath) {
        this.notebookPath = notebookPath;
    }
}
