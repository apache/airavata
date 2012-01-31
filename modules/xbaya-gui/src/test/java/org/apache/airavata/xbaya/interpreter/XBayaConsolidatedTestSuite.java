package org.apache.airavata.xbaya.interpreter;

import org.apache.airavata.xbaya.interpreter.utils.WorkflowTestUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.ListenerManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({XBayaClientTest.class, SimpleMathWorkflowTest.class, WorkflowTest.class,
        ComplexMathWorkflowTest.class, CrossProductWorkflowTest.class, ForEachWorkflowTest.class,
        WorkflowTrackingTest.class, RegistryServiceTest.class})
public class XBayaConsolidatedTestSuite {
    static ListenerManager manager = null;

    @BeforeClass
    public static void startServer() throws AxisFault {
        manager = WorkflowTestUtils.axis2ServiceStarter();
    }

    @AfterClass
    public static void stopServer() throws AxisFault {
        manager.stop();
    }

}
