package org.apache.airavata.research.service.model.repo;

import org.apache.airavata.research.service.model.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
}
