package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    
    List<Notebook> findByCategory(String category);
    
    @Query("SELECT n FROM Notebook n WHERE :tag MEMBER OF n.tags")
    List<Notebook> findByTag(@Param("tag") String tag);
    
    @Query("SELECT n FROM Notebook n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR EXISTS (SELECT t FROM n.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Notebook> findByKeyword(@Param("keyword") String keyword);
}