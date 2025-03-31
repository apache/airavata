package org.apache.airavata.research.service.model.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;

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

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.NOTEBOOK;
    }
}
