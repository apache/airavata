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
package org.apache.airavata.gfac.core.utils;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.core.states.GfacHandlerState;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.ActionableGroup;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.ErrorDetails;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.ExperimentStatus;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.JobStatus;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.airavata.commons.gfac.type.ActualParameter;

public class GFacUtils {
	private final static Logger log = LoggerFactory.getLogger(GFacUtils.class);
	public static final ArrayList<ACL> OPEN_ACL_UNSAFE = ZooDefs.Ids.OPEN_ACL_UNSAFE;

	private GFacUtils() {
	}

	/**
	 * Read data from inputStream and convert it to String.
	 * 
	 * @param in
	 * @return String read from inputStream
	 * @throws java.io.IOException
	 */
	public static String readFromStream(InputStream in) throws IOException {
		try {
			StringBuffer wsdlStr = new StringBuffer();

			int read;

			byte[] buf = new byte[1024];
			while ((read = in.read(buf)) > 0) {
				wsdlStr.append(new String(buf, 0, read));
			}
			return wsdlStr.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.warn("Cannot close InputStream: "
							+ in.getClass().getName(), e);
				}
			}
		}
	}

	/**
	 * this can be used to do framework opertaions specific to different modes
	 * 
	 * @param jobExecutionContext
	 * @return
	 */
	public static boolean isSynchronousMode(
			JobExecutionContext jobExecutionContext) {
		GFacConfiguration gFacConfiguration = jobExecutionContext
				.getGFacConfiguration();
		if (ExecutionMode.ASYNCHRONOUS.equals(gFacConfiguration
				.getExecutionMode())) {
			return false;
		}
		return true;
	}

	public static String readFileToString(String file)
			throws FileNotFoundException, IOException {
		BufferedReader instream = null;
		try {

			instream = new BufferedReader(new FileReader(file));
			StringBuffer buff = new StringBuffer();
			String temp = null;
			while ((temp = instream.readLine()) != null) {
				buff.append(temp);
				buff.append(Constants.NEWLINE);
			}
			return buff.toString();
		} finally {
			if (instream != null) {
				try {
					instream.close();
				} catch (IOException e) {
					log.warn("Cannot close FileinputStream", e);
				}
			}
		}
	}

	public static boolean isLocalHost(String appHost)
			throws UnknownHostException {
		String localHost = InetAddress.getLocalHost().getCanonicalHostName();
		return (localHost.equals(appHost)
				|| Constants.LOCALHOST.equals(appHost) || Constants._127_0_0_1
					.equals(appHost));
	}

	public static String createUniqueNameWithDate(String name) {
		String date = new Date().toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");
		return name + "_" + date;
	}

    public static List<Element> getElementList(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<Element> elementList = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item instanceof Element) {
                elementList.add((Element) item);
            }
        }
        return elementList;
    }

	public static String createGsiftpURIAsString(String host, String localPath)
			throws URISyntaxException {
		StringBuffer buf = new StringBuffer();
		if (!host.startsWith("gsiftp://"))
			buf.append("gsiftp://");
		buf.append(host);
		if (!host.endsWith("/"))
			buf.append("/");
		buf.append(localPath);
		return buf.toString();
	}

	public static void saveJobStatus(JobExecutionContext jobExecutionContext,
                                     JobDetails details, JobState state, MonitorPublisher monitorPublisher) throws GFacException {
		try {
            // first we save job details to the registry for sa and then save the job status.
            Registry registry = jobExecutionContext.getRegistry();
            JobStatus status = new JobStatus();
            status.setJobState(state);
            details.setJobStatus(status);
            registry.add(ChildDataType.JOB_DETAIL, details,
                    new CompositeIdentifier(jobExecutionContext.getTaskData()
                            .getTaskID(), details.getJobID()));
            JobIdentifier identifier = new JobIdentifier(details.getJobID(), jobExecutionContext.getTaskData().getTaskID(),
                    jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getExperimentID(),
                    jobExecutionContext.getGatewayID());
            JobStatusChangeRequestEvent jobStatusChangeRequestEvent = new JobStatusChangeRequestEvent(state, identifier);
            monitorPublisher.publish(jobStatusChangeRequestEvent);
        } catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

	public static void updateJobStatus(JobExecutionContext jobExecutionContext,
			JobDetails details, JobState state) throws GFacException {
		try {
			Registry registry = jobExecutionContext.getRegistry();
			JobStatus status = new JobStatus();
			status.setJobState(state);
			status.setTimeOfStateChange(Calendar.getInstance()
					.getTimeInMillis());
			details.setJobStatus(status);
            CompositeIdentifier identifier = new CompositeIdentifier(jobExecutionContext.getTaskData().getTaskID(), details.getJobID());
			registry.update(
					org.apache.airavata.registry.cpi.RegistryModelType.JOB_DETAIL,
					details, identifier);
		} catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

	public static void saveErrorDetails(
			JobExecutionContext jobExecutionContext, String errorMessage,
			CorrectiveAction action, ErrorCategory errorCatogory)
			throws GFacException {
		try {
			Registry registry = jobExecutionContext.getRegistry();
			ErrorDetails details = new ErrorDetails();
			details.setActualErrorMessage(errorMessage);
			details.setCorrectiveAction(action);
			details.setActionableGroup(ActionableGroup.GATEWAYS_ADMINS);
			details.setCreationTime(Calendar.getInstance().getTimeInMillis());
			details.setErrorCategory(errorCatogory);
			registry.add(ChildDataType.ERROR_DETAIL, details,
					jobExecutionContext.getTaskData().getTaskID());
		} catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

    public static Map<String, Object> getInputParamMap(List<InputDataObjectType> experimentData) throws GFacException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (InputDataObjectType objectType : experimentData) {
            map.put(objectType.getName(), objectType);
        }
        return map;
    }

    public static Map<String, Object> getOuputParamMap(List<OutputDataObjectType> experimentData) throws GFacException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (OutputDataObjectType objectType : experimentData) {
            map.put(objectType.getName(), objectType);
        }
        return map;
    }

	public static GfacExperimentState getZKExperimentState(CuratorFramework curatorClient,
			JobExecutionContext jobExecutionContext)
			throws Exception {
		String expState = AiravataZKUtils.getExpState(curatorClient, jobExecutionContext
				.getExperimentID());
        if (expState == null || expState.isEmpty()) {
            return GfacExperimentState.UNKNOWN;
        }
        return GfacExperimentState.findByValue(Integer.valueOf(expState));
    }

	public static boolean createHandlerZnode(CuratorFramework curatorClient,
                                             JobExecutionContext jobExecutionContext, String className)
			throws Exception {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), className);
		Stat exists = curatorClient.checkExists().forPath(expState);
		if (exists == null) {
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(expState, new byte[0]);
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
		} else {
			exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
			if (exists == null) {
				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
						.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
			}
		}

		exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
		if (exists != null) {
			curatorClient.setData().withVersion(exists.getVersion())
					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
							String.valueOf(GfacHandlerState.INVOKING.getValue()).getBytes());
		}
		return true;
	}

	public static boolean createHandlerZnode(CuratorFramework curatorClient,
                                             JobExecutionContext jobExecutionContext, String className,
                                             GfacHandlerState state) throws Exception {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), className);
		Stat exists = curatorClient.checkExists().forPath(expState);
		if (exists == null) {
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(expState, new byte[0]);
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
		} else {
			exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
			if (exists == null) {
				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
						.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
			}
		}

		exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
		if (exists != null) {
			curatorClient.setData().withVersion(exists.getVersion())
					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
							String.valueOf(state.getValue()).getBytes());
		}
		return true;
	}

	public static boolean updateHandlerState(CuratorFramework curatorClient,
                                             JobExecutionContext jobExecutionContext, String className,
                                             GfacHandlerState state) throws Exception {
		String handlerPath = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), className);
		Stat exists = curatorClient.checkExists().forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
		if (exists != null) {
			curatorClient.setData().withVersion(exists.getVersion())
					.forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, String.valueOf(state.getValue()).getBytes());
		} else {
			createHandlerZnode(curatorClient, jobExecutionContext, className, state);
		}
		return false;
	}

	public static GfacHandlerState getHandlerState(CuratorFramework curatorClient,
                                                  JobExecutionContext jobExecutionContext, String className) {
		try {
			String handlerPath = AiravataZKUtils.getExpZnodeHandlerPath( jobExecutionContext.getExperimentID(), className);
			Stat exists = curatorClient.checkExists().forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
			if (exists != null) {
				String stateVal = new String(curatorClient.getData().storingStatIn(exists)
						.forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE));
				return GfacHandlerState.findByValue(Integer.valueOf(stateVal));
			}
			return GfacHandlerState.UNKNOWN; // if the node doesn't exist or any other error we
							// return false
		} catch (Exception e) {
			log.error("Error occured while getting zk node status", e);
			return null;
		}
	}

	// This method is dangerous because of moving the experiment data
	public static boolean createExperimentEntryForPassive(String experimentID,
														  String taskID, CuratorFramework curatorClient, String experimentNode,
														  String pickedChild, String tokenId, long deliveryTag) throws Exception {
		String experimentPath = experimentNode + File.separator + pickedChild;
		String newExperimentPath = experimentPath + File.separator + experimentID;
		Stat exists1 = curatorClient.checkExists().forPath(newExperimentPath);
		String oldExperimentPath = GFacUtils.findExperimentEntry(experimentID, curatorClient);
		if (oldExperimentPath == null) {  // this means this is a very new experiment
			// are going to create a new node
			log.info("This is a new Job, so creating all the experiment docs from the scratch");
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(newExperimentPath, new byte[0]);
            String stateNodePath = curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(newExperimentPath + File.separator + "state",
							String .valueOf(GfacExperimentState.LAUNCHED.getValue()) .getBytes());

			if(curatorClient.checkExists().forPath(stateNodePath)!=null) {
				log.info("Created the node: " + stateNodePath + " successfully !");
			}else {
				log.error("Error creating node: " + stateNodePath + " successfully !");
			}
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag));
		} else {
			log.error("ExperimentID: " + experimentID + " taskID: " + taskID + " was running by some Gfac instance,but it failed");
            removeCancelDeliveryTagNode(oldExperimentPath, curatorClient); // remove previous cancel deliveryTagNode
            if(newExperimentPath.equals(oldExperimentPath)) {
				updateDeliveryTag(oldExperimentPath, curatorClient, deliveryTag);
				log.info("Re-launch experiment came to the same GFac instance");
			}else {
				log.info("Re-launch experiment came to a new GFac instance so we are moving data to new gfac node");
				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(newExperimentPath,
						curatorClient.getData().storingStatIn(exists1).forPath(oldExperimentPath)); // recursively copy children
                copyChildren(curatorClient, oldExperimentPath, newExperimentPath, 2); // we need to copy children up to depth 2
				String oldDeliveryTag = oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX;
				Stat exists = curatorClient.checkExists().forPath(oldDeliveryTag);
				if(exists!=null) {
					curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
							.forPath(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX,
									curatorClient.getData().storingStatIn(exists).forPath(oldDeliveryTag));
					ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), oldDeliveryTag, true);
				}
				// After all the files are successfully transfered we delete the // old experiment,otherwise we do
				// not delete a single file
				log.info("After a successful copying of experiment data for an old experiment we delete the old data");
				log.info("Deleting experiment data: " + oldExperimentPath);
				ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), oldExperimentPath, true);
			}
		}
		return true;
	}

	private static void updateDeliveryTag(String oldExperimentPath, CuratorFramework curatorClient, long deliveryTag) throws Exception {
		Stat stat = curatorClient.checkExists().forPath(oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX);
		if (stat != null) {
			curatorClient.setData().withVersion(-1).forPath(oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag));
		} else {
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag));
		}
	}

	private static void removeCancelDeliveryTagNode(String experimentPath, CuratorFramework curatorClient) throws Exception {
        Stat exists = curatorClient.checkExists().forPath(experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX);
        if (exists != null) {
			ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, true);
		}
	}

    private static void copyChildren(CuratorFramework curatorClient, String oldPath, String newPath, int depth) throws Exception {
        for (String childNode : curatorClient.getChildren().forPath(oldPath)) {
            String oldChildPath = oldPath + File.separator + childNode;
            Stat stat = curatorClient.checkExists().forPath(oldChildPath); // no need to check exists
            String newChildPath = newPath + File.separator + childNode;
            log.info("Creating new znode: " + newChildPath);
			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(newChildPath, curatorClient.getData().storingStatIn(stat).forPath(oldChildPath));
			if (--depth > 0) {
                copyChildren(curatorClient , oldChildPath, newChildPath, depth );
            }
        }
    }

	/**
	 * This will return a value if the server is down because we iterate through exisiting experiment nodes, not
	 * through gfac-server nodes
	 *
	 * @param experimentID
	 * @param curatorClient
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static String findExperimentEntry(String experimentID, CuratorFramework curatorClient) throws Exception {
		String experimentNode = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
		List<String> children = curatorClient.getChildren().forPath(experimentNode);
		for (String pickedChild : children) {
			String experimentPath = experimentNode + File.separator + pickedChild;
			String newExpNode = experimentPath + File.separator + experimentID;
			Stat exists = curatorClient.checkExists().forPath(newExpNode);
			if (exists == null) {
				continue;
			} else {
				return newExpNode;
			}
		}
		return null;
	}

    public static boolean setExperimentCancel(String experimentId, CuratorFramework curatorClient, long deliveryTag) throws Exception {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentId, curatorClient);
        if (experimentEntry == null) {
            // This should be handle in validation request. Gfac shouldn't get any invalidate experiment.
            log.error("Cannot find the experiment Entry, so cancel operation cannot be performed. " +
                    "This happen when experiment completed and already removed from the zookeeper");
            return false;
        } else {
            // check cancel operation is being processed for the same experiment.
            Stat cancelState = curatorClient.checkExists().forPath(experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX);
            if (cancelState != null) {
                // another cancel operation is being processed. only one cancel operation can exist for a given experiment.
                return false;
            }

			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
					.forPath(experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag)); // save cancel delivery tag to be acknowledge at the end.
			return true;
        }

    }
    public static boolean isCancelled(String experimentID, CuratorFramework curatorClient ) throws Exception {
		String experimentEntry = GFacUtils.findExperimentEntry(experimentID, curatorClient);
        if(experimentEntry == null){
            return false;
        }else {
            Stat exists = curatorClient.checkExists().forPath(experimentEntry);
            if (exists != null) {
				String operation = new String(curatorClient.getData().storingStatIn(exists).forPath(experimentEntry + File.separator + "operation"));
				if ("cancel".equals(operation)) {
					return true;
				}
			}
		}
        return false;
    }

    public static void saveHandlerData(JobExecutionContext jobExecutionContext,
                                       StringBuffer data, String className) throws GFacHandlerException {
		try {
			CuratorFramework curatorClient = jobExecutionContext.getCuratorClient();
			if (curatorClient != null) {
				String expZnodeHandlerPath = AiravataZKUtils
						.getExpZnodeHandlerPath(
								jobExecutionContext.getExperimentID(),
								className);
				Stat exists = curatorClient.checkExists().forPath(expZnodeHandlerPath);
                if (exists != null) {
					curatorClient.setData().withVersion(exists.getVersion()).forPath(expZnodeHandlerPath, data.toString().getBytes());
				} else {
                    log.error("Saving Handler data failed, Stat is null");
                }
            }
		} catch (Exception e) {
			throw new GFacHandlerException(e);
		}
	}

	public static String getHandlerData(JobExecutionContext jobExecutionContext, String className) throws Exception {
		CuratorFramework curatorClient = jobExecutionContext.getCuratorClient();
		if (curatorClient != null) {
			String expZnodeHandlerPath = AiravataZKUtils
					.getExpZnodeHandlerPath(
							jobExecutionContext.getExperimentID(),
							className);
			Stat exists = curatorClient.checkExists().forPath(expZnodeHandlerPath);
			return new String(jobExecutionContext.getCuratorClient().getData().storingStatIn(exists).forPath(expZnodeHandlerPath));
		}
		return null;
	}

	public static CredentialReader getCredentialReader()
			throws ApplicationSettingsException, IllegalAccessException,
			InstantiationException {
		try{
		String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
		String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
		String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
		String driver = ServerSettings.getCredentialStoreDBDriver();
		return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass,
				driver));
		}catch(ClassNotFoundException e){
			log.error("Not able to find driver: " + e.getLocalizedMessage());
			return null;	
		}
	}

    public static LOCALSubmission getLocalJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    /**
     * To convert list to separated value
     * @param listOfStrings
     * @param separator
     * @return
     */
    public static  String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for(int i = 0; i < listOfStrings.size() - 1 ; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if(listOfStrings.size() > 0){
            sb.append(listOfStrings.get(listOfStrings.size()-1));
        }

        return sb.toString();
    }

	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();//need flip
		return buffer.getLong();
	}

    public static ExperimentState updateExperimentStatus(String experimentId, ExperimentState newState) throws RegistryException {
        Registry airavataRegistry = RegistryFactory.getDefaultRegistry();
        Experiment details = (Experiment) airavataRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
        if (details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        ExperimentStatus curStatus = details.getExperimentStatus();
        ExperimentState curState = curStatus.getExperimentState();
        ExperimentStatus newStatus = new ExperimentStatus();

        newStatus.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());

        if (!curState.equals(ExperimentState.CANCELED) && !curState.equals(ExperimentState.CANCELING)) {
            newStatus.setExperimentState(newState);
        } else {
            newStatus.setExperimentState(curState);
        }

        details.setExperimentStatus(newStatus);
        if (curState.equals(newState)) {
          log.info("~~~ Updating the experiment status of experiment: " + experimentId + " to " + newStatus.getExperimentState().toString());
          airavataRegistry.update(RegistryModelType.EXPERIMENT_STATUS, newStatus, experimentId);
        } else {
          log.info("~~~ Adding a NEW experiment status of experiment: " + experimentId + " to " + newStatus.getExperimentState().toString());
          //airavataRegistry.add(ChildDataType.EXPERIMENT_STATUS, newStatus, experimentId);

          ExperimentStatus oldStatus = new ExperimentStatus();
          oldStatus.setTimeOfStateChange(curStatus.getTimeOfStateChange());
          oldStatus.setExperimentState(curStatus.getExperimentState());
          airavataRegistry.add(ChildDataType.EXPERIMENT_STATUS, oldStatus, experimentId);

          airavataRegistry.update(RegistryModelType.EXPERIMENT_STATUS, newStatus, experimentId);
        }

        return details.getExperimentStatus().getExperimentState();
    }

    public static boolean isFailedJob (JobExecutionContext jec) {
        JobStatus jobStatus = jec.getJobDetails().getJobStatus();
        if (jobStatus.getJobState() == JobState.FAILED) {
            return true;
        }
        return false;
    }

    public static boolean ackCancelRequest(String experimentId, CuratorFramework curatorClient) throws Exception {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentId, curatorClient);
        String cancelNodePath = experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX;
        if (experimentEntry == null) {
            // This should be handle in validation request. Gfac shouldn't get any invalidate experiment.
            log.error("Cannot find the experiment Entry, so cancel operation cannot be performed. " +
                    "This happen when experiment completed and already removed from the CuratorFramework");
        } else {
            // check cancel operation is being processed for the same experiment.
            Stat cancelState = curatorClient.checkExists().forPath(cancelNodePath);
            if (cancelState != null) {
				ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), cancelNodePath, true);
				return true;
			}
		}
        return false;
    }

    public static void publishTaskStatus (JobExecutionContext jobExecutionContext, MonitorPublisher publisher, TaskState state){
        TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                jobExecutionContext.getExperimentID(),
                jobExecutionContext.getGatewayID());
        publisher.publish(new TaskStatusChangeRequestEvent(state, taskIdentity));
    }
}
