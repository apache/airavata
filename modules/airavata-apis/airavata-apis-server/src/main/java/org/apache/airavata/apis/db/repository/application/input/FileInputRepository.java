package org.apache.airavata.apis.db.repository.application.input;

import org.apache.airavata.apis.db.entity.application.input.FileInputEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInputRepository extends CrudRepository<FileInputEntity, String> {
}
