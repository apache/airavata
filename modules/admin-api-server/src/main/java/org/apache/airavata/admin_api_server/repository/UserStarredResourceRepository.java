package org.apache.airavata.admin_api_server.repository;

import org.apache.airavata.admin_api_server.entity.User;
import org.apache.airavata.admin_api_server.entity.UserStarredResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStarredResourceRepository extends JpaRepository<UserStarredResource, Long> {
    Optional<UserStarredResource> findByUserAndResourceIdAndResourceType(
        User user, Long resourceId, UserStarredResource.ResourceType resourceType);
    
    List<UserStarredResource> findByUser(User user);
    
    void deleteByUserAndResourceIdAndResourceType(
        User user, Long resourceId, UserStarredResource.ResourceType resourceType);
    
    @Query("SELECT COUNT(usr) > 0 FROM UserStarredResource usr WHERE usr.user = :user AND usr.resourceId = :resourceId AND usr.resourceType = :resourceType")
    boolean existsByUserAndResourceIdAndResourceType(
        @Param("user") User user, 
        @Param("resourceId") Long resourceId, 
        @Param("resourceType") UserStarredResource.ResourceType resourceType);
}