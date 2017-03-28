package org.apache.airavata.sharing.registry.utils;

import org.apache.airavata.common.utils.DBEventService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class Constants {
    /**
     * List of publishers in which sharing service is interested.
     * Add publishers as required
     */
    public static final List<String> PUBLISHERS = new ArrayList<String>(){{add(DBEventService.USER_PROFILE.toString());};};
}
