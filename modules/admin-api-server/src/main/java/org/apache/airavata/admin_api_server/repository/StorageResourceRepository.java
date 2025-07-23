package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.StorageResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageResourceRepository extends JpaRepository<StorageResource, Long> {
    
    @Query("SELECT sr FROM StorageResource sr WHERE " +
           "LOWER(sr.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sr.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sr.storage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sr.storageType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sr.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StorageResource> findByKeyword(@Param("keyword") String keyword);
}