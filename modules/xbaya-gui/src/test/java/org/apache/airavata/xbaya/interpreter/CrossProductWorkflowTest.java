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

/**
 * Created by IntelliJ IDEA.
 * User: heshan
 * Date: 12/18/11
 * Time: 8:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrossProductWorkflowTest {
    @Test
    public void testScheduleDynamically() throws IOException, URISyntaxException, XBayaException {
        URL systemResource = this.getClass().getClassLoader().getSystemResource("foreach-cross-product-levenshtein-distance.xwf");
        Workflow workflow = new Workflow(WorkflowTestUtils.readWorkflow(systemResource));
        ListenerManager manager = WorkflowTestUtils.axis2ServiceStarter();
//        ((InputNode) workflow.getGraph().getNode("Input")).setDefaultValue("abc");
//        ((InputNode) workflow.getGraph().getNode("Input_2")).setDefaultValue("def");
        WorkflowInterpreter interpretor = new WorkflowInterpreter(WorkflowTestUtils.getConfiguration(), UUID.randomUUID().toString(),
                workflow, "NA", "NA",true);
        interpretor.scheduleDynamically();
        manager.stop();
    }
}
