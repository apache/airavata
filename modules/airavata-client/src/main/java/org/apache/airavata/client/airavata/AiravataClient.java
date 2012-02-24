/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.client.airavata;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.WorkflowExecution;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.AxisFault;
import xsul5.MLogger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class AiravataClient {
    private static final MLogger log = MLogger.getLogger();

    public static final String GFAC = "gfac";
    public static final String JCR = "jcr";
    public static final String PROXYSERVER = "proxyserver";
    public static final String MSGBOX = "msgbox";
    public static final String BROKER = "broker";
    public static final String DEFAULT_GFAC_URL = "gfac.url";
    public static final String DEFAULT_MYPROXY_SERVER = "myproxy.url";
    public static final String DEFAULT_MESSAGE_BOX_URL = "messagebox.url";
    public static final String DEFAULT_BROKER_URL = "messagebroker.url";
    public static final String DEFAULT_JCR_URL = "jcr.url";
    public static final String JCR_USERNAME = "jcr.username";
    public static final String JCR_PASSWORD= "jcr.password";
    public static final String MYPROXYUSERNAME = "myproxy.username";
    public static final String MYPROXYPASS = "myproxy.password";
    public static final String WITHLISTENER = "with.Listener";
    public static final String WORKFLOWSERVICEURL = "xbaya.service.url";
    private AiravataClientConfiguration clientConfiguration;
    private static String workflow = "";

    private AiravataRegistry registry;

	private NameValue[] configurations=new NameValue[11];
    
//    private NameValue[] configurations = new NameValue[7];

    public AiravataClient(NameValue[] configuration) throws MalformedURLException {
        configurations = configuration;
        updateClientConfiguration(configurations);
    }

    public AiravataClient(String fileName) throws RegistryException,MalformedURLException,IOException{
        URL url = this.getClass().getClassLoader().getResource(fileName);
        if(url == null){
            url = (new File(fileName)).toURL();
        }
        Properties properties = new Properties();
        properties.load(url.openStream());

        configurations[0] = new NameValue();
        configurations[0].setName(GFAC);
        configurations[0].setValue(validateAxisService(properties.getProperty(DEFAULT_GFAC_URL)));
        
        configurations[1] = new NameValue();
        configurations[1].setName(PROXYSERVER);
        configurations[1].setValue(properties.getProperty(DEFAULT_MYPROXY_SERVER));
        
        configurations[2] = new NameValue();
        configurations[2].setName(MSGBOX);
        configurations[2].setValue(validateAxisService(properties.getProperty(DEFAULT_MESSAGE_BOX_URL)));

        configurations[3] = new NameValue();
        configurations[3].setName(BROKER);
        configurations[3].setValue(validateAxisService(properties.getProperty(DEFAULT_BROKER_URL)));

        configurations[4] = new NameValue();
        configurations[4].setName(MYPROXYUSERNAME);
        configurations[4].setValue(properties.getProperty(MYPROXYUSERNAME));

        configurations[5] = new NameValue();
        configurations[5].setName(MYPROXYPASS);
        configurations[5].setValue(properties.getProperty(MYPROXYPASS));

        configurations[6] = new NameValue();
        configurations[6].setName(WORKFLOWSERVICEURL);
        configurations[6].setValue(validateAxisService(properties.getProperty(WORKFLOWSERVICEURL)));
        
        configurations[7] = new NameValue();
        configurations[7].setName(JCR);
        configurations[7].setValue(validateURL(properties.getProperty(DEFAULT_JCR_URL)));
        
        configurations[8] = new NameValue();
        configurations[8].setName(JCR_USERNAME);
        configurations[8].setValue(properties.getProperty(JCR_USERNAME));
        
        configurations[9] = new NameValue();
        configurations[9].setName(JCR_PASSWORD);
        configurations[9].setValue(properties.getProperty(JCR_PASSWORD));

        configurations[10] = new NameValue();
        configurations[10].setName(WITHLISTENER);
        configurations[10].setValue(properties.getProperty(WITHLISTENER));

        updateClientConfiguration(configurations);

    }

    private void updateClientConfiguration(NameValue[] configurations) throws MalformedURLException {
		AiravataClientConfiguration clientConfiguration = getClientConfiguration();
    	for (NameValue configuration : configurations) {
			if (configuration.getName().equals(GFAC)){
    			clientConfiguration.setJcrURL(new URL(configuration.getValue()));
    		}
    		if (configuration.getName().equals(PROXYSERVER)){
    			clientConfiguration.setMyproxyHost(configuration.getValue());
    		}
    		if (configuration.getName().equals(MSGBOX)){
    			clientConfiguration.setMessageboxURL(new URL(configuration.getValue()));
    		}
    		if (configuration.getName().equals(BROKER)){
    			clientConfiguration.setMessagebrokerURL(new URL(configuration.getValue()));
    		}
    		if (configuration.getName().equals(MYPROXYUSERNAME)){
    			clientConfiguration.setMyproxyUsername(configuration.getValue());
    		}
    		if (configuration.getName().equals(MYPROXYPASS)){
    			clientConfiguration.setMyproxyPassword(configuration.getValue());
    		}
    		if (configuration.getName().equals(WORKFLOWSERVICEURL)){
    			clientConfiguration.setXbayaServiceURL(new URL(configuration.getValue()));
    		}
    		if (configuration.getName().equals(JCR)){
    			clientConfiguration.setJcrURL(new URL(configuration.getValue()));
    		}
    		if (configuration.getName().equals(JCR_USERNAME)){
    			clientConfiguration.setJcrUsername(configuration.getValue());
    		}
    		if (configuration.getName().equals(JCR_PASSWORD)){
    			clientConfiguration.setJcrPassword(configuration.getValue());
    		}
    	}

	}
    public void loadWorkflowFromaFile(String workflowFile)throws URISyntaxException,IOException {
        File workflow = null;
        URL url = AiravataClient.class.getClassLoader().getResource(workflowFile);
        if(url == null){
            url = (new File(workflowFile)).toURL();
        }
        try {
            workflow = new File(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        FileInputStream stream = new FileInputStream(workflow);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            this.workflow = Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    public void loadWorkflowasaString(String workflowAsaString) {
        this.workflow = workflowAsaString;
    }


    public NameValue[] setInputs(String fileName) throws IOException {
        URL url = this.getClass().getClassLoader().getResource(fileName);
        if(url == null){
            url = (new File(fileName)).toURL();
        }
        Properties properties = new Properties();
        properties.load(url.openStream());
        try {
            Workflow workflow = new Workflow(this.workflow);
            List<NodeImpl> inputs = workflow.getGraph().getNodes();
            int inputSize = 0;
            for (NodeImpl input : inputs) {
                if(input instanceof InputNode){
                    inputSize++;
                }
            }
            NameValue[] inputNameVals = new NameValue[inputSize];
            int index =0;
            for (NodeImpl input : inputs) {
                if(input instanceof InputNode){
                    inputNameVals[index] = new NameValue();
                    String name = input.getName();
                    inputNameVals[index].setName(name);
                    inputNameVals[index].setValue(properties.getProperty(name));
                    ((InputNode) input).setDefaultValue(properties.getProperty(name));
                    index++;
                }
            }
//            setWorkflow(XMLUtil.xmlElementToString((workflow.toXML()));
            return inputNameVals;
        } catch (GraphException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ComponentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public void setInputs(Properties inputList){
       try {
            Workflow workflow = new Workflow(this.workflow);
            List<WSComponentPort> inputs = workflow.getInputs();
            for (WSComponentPort input : inputs) {
                input.setValue(inputList.getProperty(input.getName()));
            }
        } catch (GraphException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ComponentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public String runWorkflow(String topic){
    	return runWorkflow(topic,(String)null);
    }
    
    public String runWorkflow(String topic, String user){
    	return runWorkflow(topic, user, null);
    }
    public String runWorkflow(String topic, String user, String metadata){
		String worflowoutput= null;
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(getClientConfiguration().getXbayaServiceURL().toString());
		    worflowoutput = stub.launchWorkflow(workflow, topic, getClientConfiguration().getMyproxyPassword(),getClientConfiguration().getMyproxyUsername(), null,
					configurations);
		    runPostWorkflowExecutionTasks(topic, user, metadata);
			log.info("Workflow output : " + worflowoutput);
		} catch (AxisFault e) {
			log.fine(e.getMessage(), e);
		} catch (RemoteException e) {
			log.fine(e.getMessage(), e);
		} catch (RegistryException e) {
			log.fine(e.getMessage(), e);
		}
		return worflowoutput;
	}

	private void runPostWorkflowExecutionTasks(String topic, String user,
			String metadata) throws RegistryException {
		if (user!=null) {
			getRegistry().saveWorkflowExecutionUser(topic, user);
		}
		if (metadata!=null){
			getRegistry().saveWorkflowExecutionMetadata(topic, metadata);
		}
	}
    public String runWorkflow(String topic, NameValue[] inputs){
    	return runWorkflow(topic, inputs, null);
    }
    public String runWorkflow(String topic, NameValue[] inputs, String user){
    	return runWorkflow(topic, inputs, user, null);
    }
    public String runWorkflow(String topic, NameValue[] inputs, String user, String metadata){
		String worflowoutput= null;
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(getClientConfiguration().getXbayaServiceURL().toString());
		    worflowoutput = stub.launchWorkflow(workflow, topic, getClientConfiguration().getMyproxyPassword(),getClientConfiguration().getMyproxyUsername(), inputs,
					configurations);
		    runPostWorkflowExecutionTasks(topic, user, metadata);
			log.info("Workflow output : " + worflowoutput);
		} catch (AxisFault e) {
			log.fine(e.getMessage(), e);
		} catch (RemoteException e) {
			log.fine(e.getMessage(), e);
		} catch (RegistryException e) {
			log.fine(e.getMessage(), e);
		}
		return worflowoutput;
	}

    
    public List<WorkflowExecution> getWorkflowExecutionDataByUser(String user) throws RegistryException{
    	return getRegistry().getWorkflowExecutionByUser(user);
    }
    
    public WorkflowExecution getWorkflowExecutionData(String topic) throws RegistryException{
    	return getRegistry().getWorkflowExecution(topic);
    }
    
    /**
     * 
     * @param user
     * @param pageSize - number of executions to return (page size)
     * @param PageNo - which page to return to (>=0) 
     * @return
     * @throws RegistryException
     */
    public List<WorkflowExecution> getWorkflowExecutionData(String user, int pageSize, int PageNo) throws RegistryException{
    	return getRegistry().getWorkflowExecutionByUser(user, pageSize, PageNo);
    }
    
    public static String getWorkflow() {
        return workflow;
    }

    public static void setWorkflow(String workflow) {
        AiravataClient.workflow = workflow;
    }

	public AiravataRegistry getRegistry() {
		if (registry==null){
			try {
				HashMap<String, String> map = new HashMap<String, String>();
		        URI uri = getClientConfiguration().getJcrURL().toURI();
				map.put("org.apache.jackrabbit.repository.uri", uri.toString());
				registry=new AiravataJCRRegistry(uri, "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory", getClientConfiguration().getJcrUsername(), getClientConfiguration().getJcrPassword(), map);
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return registry;
	}

	public AiravataClientConfiguration getClientConfiguration() {
		if (clientConfiguration==null){
			clientConfiguration=new AiravataClientConfiguration();
		}
		return clientConfiguration;
	}

    private String validateAxisService(String urlString)throws RegistryException{
        if(!urlString.endsWith("?wsdl")){
            urlString = urlString + "?wsdl";
        }
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            // the URL is not in a valid form
            throw new RegistryException("Given Axis2 Service URL : " + urlString + " is Invalid",e);
        } catch (IOException e) {
            // the connection couldn't be established
            throw new RegistryException("Given Axis2 Service URL : " + urlString + " is Invalid",e);
        }
        return  urlString;
    }
    private String validateURL(String urlString)throws RegistryException{
           try {
               URL url = new URL(urlString);
               URLConnection conn = url.openConnection();
               conn.connect();
           } catch (MalformedURLException e) {
               // the URL is not in a valid form
               throw new RegistryException("Given URL: " + urlString + " is Invalid",e);
           } catch (IOException e) {
               // the connection couldn't be established
               throw new RegistryException("Given URL: " + urlString + " is Invalid",e);
           }
           return  urlString;
       }

}

