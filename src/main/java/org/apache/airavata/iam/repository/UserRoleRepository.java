package org.apache.airavata.iam.repository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.apache.airavata.iam.model.UserRoleEntity;
import org.apache.airavata.iam.model.UserRoleId;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, UserRoleId> {

}
