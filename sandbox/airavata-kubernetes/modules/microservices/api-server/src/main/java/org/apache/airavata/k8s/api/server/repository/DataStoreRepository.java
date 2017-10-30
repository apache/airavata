package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.data.DataStoreModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface DataStoreRepository extends CrudRepository<DataStoreModel, Long>{

    List<DataStoreModel> findByTaskModel_ParentProcess_Id(long processId);
}
