package org.apache.airavata.helix.core.support;

import org.apache.airavata.helix.task.api.TaskHelper;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskHelperImpl implements TaskHelper {

    public AdaptorSupportImpl getAdaptorSupport() {
        return AdaptorSupportImpl.getInstance();
    }
}
