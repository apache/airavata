package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    
    List<Model> findByCategory(String category);
    
    @Query("SELECT m FROM Model m WHERE :tag MEMBER OF m.tags")
    List<Model> findByTag(@Param("tag") String tag);
    
    @Query("SELECT m FROM Model m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR EXISTS (SELECT t FROM m.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Model> findByKeyword(@Param("keyword") String keyword);
}