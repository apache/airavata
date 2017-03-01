package org.apache.airavata.db.event.manager.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ajinkya on 3/1/17.
 */
public class Constants {

    //FIXME: value can be list of String or list of publisher objects
    public static final Map<String, List<String>> DB_EVENT_MAP;
    static{
        //populate map based on ZK folder structure (cache)
        DB_EVENT_MAP = new HashMap<>();
    }
}
