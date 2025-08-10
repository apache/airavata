package org.apache.airavata.research.service.model.repo;

import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.ResourceVerificationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ResourceVerificationActivityRepository extends JpaRepository<ResourceVerificationActivity, String>, JpaSpecificationExecutor<ResourceVerificationActivity> {

    List<ResourceVerificationActivity> findAllByResourceOrderByUpdatedAtDesc(Resource resource);

}
