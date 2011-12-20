package org.apache.airavata.xbaya.interpreter;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.interpreter.utils.WorkflowTestUtils;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.engine.ListenerManager;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class SimpleMathWorkflowTest {

      @Test
    public void testScheduleDynamically() throws IOException, URISyntaxException, XBayaException {
        URL systemResource = this.getClass().getClassLoader().getSystemResource("SimpleMath.xwf");
        Workflow workflow = new Workflow(WorkflowTestUtils.readWorkflow(systemResource));
        ListenerManager manager = WorkflowTestUtils.axis2ServiceStarter();
        WorkflowInterpreter interpretor = new WorkflowInterpreter(WorkflowTestUtils.getConfiguration(), UUID.randomUUID().toString(),
                workflow, "NA", "NA",true);
        interpretor.scheduleDynamically();
        manager.stop();
    }

}
