package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.ComputeResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComputeResourceRepository extends JpaRepository<ComputeResource, Long> {
    
    @Query("SELECT cr FROM ComputeResource cr WHERE " +
           "LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cr.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cr.compute) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cr.computeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cr.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ComputeResource> findByKeyword(@Param("keyword") String keyword);
}