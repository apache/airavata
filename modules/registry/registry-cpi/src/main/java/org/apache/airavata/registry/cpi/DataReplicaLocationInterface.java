package org.apache.airavata.registry.cpi;

import org.apache.airavata.model.data.replica.DataReplicaLocationModel;

import java.util.List;

public interface DataReplicaLocationInterface {

    String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException;

    boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException;

    DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException;

    List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException;

    boolean removeReplicaLocation(String replicaId) throws ReplicaCatalogException;

}
