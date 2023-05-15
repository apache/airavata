package org.apache.airavata.apis.db.repository.application.input;

import org.apache.airavata.apis.db.entity.application.input.ApplicationInputEntity;
import org.apache.airavata.apis.db.entity.application.input.CommandLineInputEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandLineInputRepository extends CrudRepository<CommandLineInputEntity, String> {
}
