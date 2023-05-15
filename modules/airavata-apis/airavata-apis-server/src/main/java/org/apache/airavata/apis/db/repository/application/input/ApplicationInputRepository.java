package org.apache.airavata.apis.db.repository.application.input;


import org.apache.airavata.apis.db.entity.application.input.ApplicationInputEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationInputRepository extends CrudRepository<ApplicationInputEntity, String> {
}
