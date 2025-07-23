package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    
    List<Repository> findByCategory(String category);
    
    @Query("SELECT r FROM Repository r WHERE :tag MEMBER OF r.tags")
    List<Repository> findByTag(@Param("tag") String tag);
    
    @Query("SELECT r FROM Repository r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR EXISTS (SELECT t FROM r.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Repository> findByKeyword(@Param("keyword") String keyword);
}