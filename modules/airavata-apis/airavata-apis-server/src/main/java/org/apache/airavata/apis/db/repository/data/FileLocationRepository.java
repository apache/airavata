package org.apache.airavata.apis.db.repository.data;

import org.apache.airavata.apis.db.entity.data.FileLocationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLocationRepository extends CrudRepository<FileLocationEntity, String> {
}
