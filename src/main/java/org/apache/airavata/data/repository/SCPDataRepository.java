package org.apache.airavata.data.repository;

import java.util.List;
import org.apache.airavata.data.model.SCPDataEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SCPDataRepository extends ListCrudRepository<SCPDataEntity, String> {

    List<SCPDataEntity> findByOwner_UserId(String userId);
}
