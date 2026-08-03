package org.apache.airavata.iam.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.apache.airavata.iam.model.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {

}
