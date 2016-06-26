
package org.apache.airavata.cloud.aurora.bigDataInjections;

import java.util.Map;
import java.util.List;


public interface BigDataInjectorI {

    // TODO: this interface should throw an exception
    public void executeTheBigDataClientSideCommand(Map<String, List<String>> commandLineOptions);
}
