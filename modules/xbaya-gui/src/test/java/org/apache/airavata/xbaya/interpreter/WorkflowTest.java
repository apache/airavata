package org.apache.airavata.xbaya.interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.interpretor.HeaderConstants;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorSkeleton;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorStub.NameValue;
import org.apache.airavata.xbaya.wf.Workflow;
import org.junit.Test;
import org.python.antlr.PythonParser.classdef_return;

public class WorkflowTest implements HeaderConstants {

	@Test
	public void testScheduleDynamically() throws IOException, URISyntaxException, XBayaException {
		
		Workflow workflow = new Workflow(readWorkflow());
		((InputNode)workflow.getGraph().getNode("before")).setDefaultValue("1");
		WorkflowInterpreter interpretor = new WorkflowInterpreter(getConfiguration(), UUID.randomUUID().toString(), workflow, "NA", "NA", true);
		interpretor.scheduleDynamically();
	}
	
	
	
	private XBayaConfiguration getConfiguration() throws URISyntaxException {
		NameValue[] configurations = new NameValue[6];
        configurations[0] = new NameValue();
        configurations[0].setName(HEADER_ELEMENT_GFAC);
        configurations[0].setValue(XBayaConstants.DEFAULT_GFAC_URL.toString());
        configurations[1] = new NameValue();
        configurations[1].setName(HEADER_ELEMENT_XREGISTRY);
        configurations[1].setValue(XBayaConstants.DEFAULT_XREGISTRY_URL.toString());
        configurations[2] = new NameValue();
        configurations[2].setName(HEADER_ELEMENT_PROXYSERVER);
        configurations[2].setValue(XBayaConstants.DEFAULT_MYPROXY_SERVER);

        configurations[3] = new NameValue();
        configurations[3].setName(HEADER_ELEMENT_MSGBOX);
        configurations[3].setValue(XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString());

        configurations[4] = new NameValue();
        configurations[4].setName(HEADER_ELEMENT_DSC);
        configurations[4].setValue(XBayaConstants.DEFAULT_DSC_URL.toString());

        configurations[5] = new NameValue();
        configurations[5].setName(HEADER_ELEMENT_BROKER);
        configurations[5].setValue(XBayaConstants.DEFAULT_BROKER_URL.toString());
		return WorkflowInterpretorSkeleton.getConfiguration(configurations);
	}



	private String readWorkflow() throws IOException, URISyntaxException{
		
		URL url = this.getClass().getClassLoader().getSystemResource("test.xwf");
		FileInputStream stream = new FileInputStream(new File(url.toURI()));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }

	}

}
