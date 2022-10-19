package org.apache.airavata.metascheduler.core.engine;

import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;

import java.util.Optional;

/**
 * This interface provides apis to implement for select compute resources
 * from compute resource pool according to different selection strategies
 */
public interface ComputeResourceSelectionPolicy {


    /**
     * This interface implements compute resource selection
     * @param experimentId
     * @return Optional<ComputationalResourceSchedulingModel>
     */
    Optional<ComputationalResourceSchedulingModel> selectComputeResource(String experimentId);




}
