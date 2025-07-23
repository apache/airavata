package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    
    List<Dataset> findByCategory(String category);
    
    @Query("SELECT d FROM Dataset d WHERE :tag MEMBER OF d.tags")
    List<Dataset> findByTag(@Param("tag") String tag);
    
    @Query("SELECT d FROM Dataset d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR EXISTS (SELECT t FROM d.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Dataset> findByKeyword(@Param("keyword") String keyword);
}