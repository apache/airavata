package org.apache.airavata.research.service.model.repo;
import org.apache.airavata.research.service.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.apache.airavata.research.service.model.entity.NotebookResource;

public interface TagRepository extends JpaRepository<Tag, String> {
    Tag findByValue(String value);
}
