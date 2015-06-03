package org.apache.airavata.gfac.monitor.impl.push.amqp;

import java.util.List;

/**
 * Created by syodage on 6/3/15.
 */
public class ComputingActivity {
    String idFromEndpoint;
    private List<String> state;

    public String getIDFromEndpoint() {
        return idFromEndpoint;
    }

    public List<String> getState() {
        return state;
    }
}
