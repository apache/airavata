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
import org.apache.airavata.messaging.core.impl.RabbitMQTaskLaunchConsumer;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

//import org.apache.airavata.commons.gfac.type.ActualParameter;

public class GFacUtils {
	private final static Logger log = LoggerFactory.getLogger(GFacUtils.class);

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
			registry.update(
					org.apache.airavata.registry.cpi.RegistryModelType.JOB_DETAIL,
					details, details.getJobID());
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

	public static GfacExperimentState getZKExperimentState(ZooKeeper zk,
			JobExecutionContext jobExecutionContext)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpState(zk, jobExecutionContext
				.getExperimentID());
        if (expState == null || expState.isEmpty()) {
            return GfacExperimentState.UNKNOWN;
        }
        return GfacExperimentState.findByValue(Integer.valueOf(expState));
    }

	public static int getZKExperimentStateValue(ZooKeeper zk,
			JobExecutionContext jobExecutionContext)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpState(zk, jobExecutionContext
				.getExperimentID());
		if (expState == null) {
			return -1;
		}
		return Integer.parseInt(expState);
	}

    public static int getZKExperimentStateValue(ZooKeeper zk,String fullPath)throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(fullPath+File.separator+"state", false);
        if (exists != null) {
            return Integer.parseInt(new String(zk.getData(fullPath+File.separator+"state", false, exists)));
        }
        return -1;
    }

	public static boolean createHandlerZnode(ZooKeeper zk,
                                             JobExecutionContext jobExecutionContext, String className)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), className);
		Stat exists = zk.exists(expState, false);
		if (exists == null) {
			zk.create(expState, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			zk.create(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0],
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} else {
			exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists == null) {
				zk.create(expState + File.separator
						+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
						new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		}

		exists = zk.exists(expState + File.separator
				+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
		if (exists != null) {
			zk.setData(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
					String.valueOf(GfacHandlerState.INVOKING.getValue())
							.getBytes(), exists.getVersion());
		}
		return true;
	}

	public static boolean createHandlerZnode(ZooKeeper zk,
                                             JobExecutionContext jobExecutionContext, String className,
                                             GfacHandlerState state) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), className);
		Stat exists = zk.exists(expState, false);
		if (exists == null) {
			zk.create(expState, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			zk.create(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0],
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} else {
			exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists == null) {
				zk.create(expState + File.separator
						+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
						new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		}

		exists = zk.exists(expState + File.separator
				+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
		if (exists != null) {
			zk.setData(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
					String.valueOf(state.getValue()).getBytes(),
					exists.getVersion());
		}
		return true;
	}

	public static boolean updateHandlerState(ZooKeeper zk,
                                             JobExecutionContext jobExecutionContext, String className,
                                             GfacHandlerState state) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		if(zk.getState().isConnected()) {
			String expState = AiravataZKUtils.getExpZnodeHandlerPath(
					jobExecutionContext.getExperimentID(), className);

			Stat exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists != null) {
				zk.setData(expState + File.separator
								+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
						String.valueOf(state.getValue()).getBytes(),
						exists.getVersion());
			} else {
				createHandlerZnode(zk, jobExecutionContext, className, state);
			}
			return true;
		}
		return false;
	}

	public static GfacHandlerState getHandlerState(ZooKeeper zk,
                                                  JobExecutionContext jobExecutionContext, String className) {
		try {
			String expState = AiravataZKUtils.getExpZnodeHandlerPath(
					jobExecutionContext.getExperimentID(), className);

			Stat exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists != null) {
                String stateVal = new String(zk.getData(expState + File.separator
                                + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false,
                        exists));
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
														  String taskID, ZooKeeper zk, String experimentNode,
														  String pickedChild, String tokenId, long deliveryTag) throws KeeperException,
			InterruptedException, ApplicationSettingsException {
		String experimentPath = experimentNode + File.separator + pickedChild;
		String newExperimentPath = experimentPath + File.separator + experimentID;
		Stat exists1 = zk.exists(newExperimentPath, false);
		String oldExperimentPath = GFacUtils.findExperimentEntry(experimentID, zk);
		if (oldExperimentPath == null) {  // this means this is a very new experiment
			// are going to create a new node
			log.info("This is a new Job, so creating all the experiment docs from the scratch");

			zk.create(newExperimentPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

            String s = zk.create(newExperimentPath + File.separator + "state", String
							.valueOf(GfacExperimentState.LAUNCHED.getValue())
							.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			if(zk.exists(s,false)!=null){
				log.info("Created the node: "+s+" successfully !");
			}else{
				log.error("Error creating node: "+s+" successfully !");
			}
			zk.create(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag), ZooDefs.Ids.OPEN_ACL_UNSAFE,  // here we store the value of delivery message
					CreateMode.PERSISTENT);
		} else {
			log.error("ExperimentID: " + experimentID + " taskID: " + taskID + " was running by some Gfac instance,but it failed");
            removeCancelDeliveryTagNode(oldExperimentPath, zk); // remove previous cancel deliveryTagNode
            if(newExperimentPath.equals(oldExperimentPath)){
                log.info("Re-launch experiment came to the same GFac instance");
            }else {
				log.info("Re-launch experiment came to a new GFac instance so we are moving data to new gfac node");
				zk.create(newExperimentPath, zk.getData(oldExperimentPath, false, exists1),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // recursively copy children
                copyChildren(zk, oldExperimentPath, newExperimentPath, 2); // we need to copy children up to depth 2
				String oldDeliveryTag = oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX;
				Stat exists = zk.exists(oldDeliveryTag, false);
				if(exists!=null) {
					zk.create(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX,
							zk.getData(oldDeliveryTag,null,exists),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					ZKUtil.deleteRecursive(zk,oldDeliveryTag);
				}
				// After all the files are successfully transfered we delete the // old experiment,otherwise we do
				// not delete a single file
				log.info("After a successful copying of experiment data for an old experiment we delete the old data");
				log.info("Deleting experiment data: " + oldExperimentPath);
				ZKUtil.deleteRecursive(zk, oldExperimentPath);
			}
		}
		return true;
	}

    private static void removeCancelDeliveryTagNode(String experimentPath, ZooKeeper zk) throws KeeperException, InterruptedException {
        Stat exists = zk.exists(experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, false);
        if (exists != null) {
            ZKUtil.deleteRecursive(zk, experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX);
        }
    }

    private static void copyChildren(ZooKeeper zk, String oldPath, String newPath, int depth) throws KeeperException, InterruptedException {
        for (String childNode : zk.getChildren(oldPath, false)) {
            String oldChildPath = oldPath + File.separator + childNode;
            Stat stat = zk.exists(oldChildPath, false); // no need to check exists
            String newChildPath = newPath + File.separator + childNode;
            log.info("Creating new znode: " + newChildPath);
            zk.create(newChildPath, zk.getData(oldChildPath, false, stat), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            if (--depth > 0) {
                copyChildren(zk , oldChildPath, newChildPath, depth );
            }
        }
    }

    /**
	 * This will return a value if the server is down because we iterate through exisiting experiment nodes, not
	 * through gfac-server nodes
	 * @param experimentID
	 * @param zk
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
    public static String findExperimentEntry(String experimentID, ZooKeeper zk
                                                ) throws KeeperException,
            InterruptedException {
        String experimentNode = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
        List<String> children = zk.getChildren(experimentNode, false);
        for(String pickedChild:children) {
            String experimentPath = experimentNode + File.separator + pickedChild;
            String newExpNode = experimentPath + File.separator + experimentID;
            Stat exists = zk.exists(newExpNode, false);
            if(exists == null){
                continue;
            }else{
                return newExpNode;
            }
        }
        return null;
    }

	/**
	 * This will return a value if the server is down because we iterate through exisiting experiment nodes, not
	 * through gfac-server nodes
	 * @param experimentID
	 * @param zk
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static String findExperimentEntryPassive(String experimentID, ZooKeeper zk
	) throws KeeperException,
			InterruptedException {
		String experimentNode = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
		List<String> children = zk.getChildren(experimentNode, false);
		for(String pickedChild:children) {
			String experimentPath = experimentNode + File.separator + pickedChild;
			String newExpNode = experimentPath + File.separator + experimentID;
			Stat exists = zk.exists(newExpNode, false);
			if(exists == null){
				continue;
			}else{
				return newExpNode;
			}
		}
		return null;
	}

    public static boolean setExperimentCancel(String experimentId, ZooKeeper zk, long deliveryTag) throws KeeperException,
            InterruptedException {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentId, zk);
        if (experimentEntry == null) {
            // This should be handle in validation request. Gfac shouldn't get any invalidate experiment.
            log.error("Cannot find the experiment Entry, so cancel operation cannot be performed. " +
                    "This happen when experiment completed and already removed from the zookeeper");
            return false;
        } else {
            // check cancel operation is being processed for the same experiment.
            Stat cancelState = zk.exists(experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, false);
            if (cancelState != null) {
                // another cancel operation is being processed. only one cancel operation can exist for a given experiment.
                return false;
            }

            zk.create(experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // save cancel delivery tag to be acknowledge at the end.
            return true;
        }

    }
    public static boolean isCancelled(String experimentID, ZooKeeper zk
    ) throws KeeperException,
            InterruptedException {
		String experimentEntry = GFacUtils.findExperimentEntry(experimentID, zk);

        if(experimentEntry == null){
            return false;
        }else {
            Stat exists = zk.exists(experimentEntry, false);
            if (exists != null) {
                String operation = new String(zk.getData(experimentEntry+File.separator+"operation", false, exists));
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
			ZooKeeper zk = jobExecutionContext.getZk();
			if (zk != null) {
				String expZnodeHandlerPath = AiravataZKUtils
						.getExpZnodeHandlerPath(
								jobExecutionContext.getExperimentID(),
								className);
				Stat exists = zk.exists(expZnodeHandlerPath, false);
                if (exists != null) {
                    zk.setData(expZnodeHandlerPath, data.toString().getBytes(),
                            exists.getVersion());
                } else {
                    log.error("Saving Handler data failed, Stat is null");
                }
            }
		} catch (Exception e) {
			throw new GFacHandlerException(e);
		}
	}

	public static String getHandlerData(JobExecutionContext jobExecutionContext,
                                        String className) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		ZooKeeper zk = jobExecutionContext.getZk();
		if (zk != null) {
			String expZnodeHandlerPath = AiravataZKUtils
					.getExpZnodeHandlerPath(
							jobExecutionContext.getExperimentID(),
							className);
			Stat exists = zk.exists(expZnodeHandlerPath, false);
			return new String(jobExecutionContext.getZk().getData(
					expZnodeHandlerPath, false, exists));
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

    public static GlobusJobSubmission getGlobusJobSubmission (String submissionId) throws AppCatalogException{
        return null;
//        try {
//            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
//            return appCatalog.getComputeResource().getGlobus(submissionId);
//        }catch (Exception e){
//            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
//            log.error(errorMsg, e);
//            throw new AppCatalogException(errorMsg, e);
//        }
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

    public static CloudJobSubmission getCloudJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getCloudJobSubmission(submissionId);
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

    public static ExperimentState updateExperimentStatus(String experimentId, ExperimentState state) throws RegistryException {
        Registry airavataRegistry = RegistryFactory.getDefaultRegistry();
        Experiment details = (Experiment) airavataRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
        if (details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        org.apache.airavata.model.workspace.experiment.ExperimentStatus status = new org.apache.airavata.model.workspace.experiment.ExperimentStatus();
        status.setExperimentState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        if (!ExperimentState.CANCELED.equals(details.getExperimentStatus().getExperimentState()) &&
                !ExperimentState.CANCELING.equals(details.getExperimentStatus().getExperimentState())) {
            status.setExperimentState(state);
        } else {
            status.setExperimentState(details.getExperimentStatus().getExperimentState());
        }
        details.setExperimentStatus(status);
        log.info("Updating the experiment status of experiment: " + experimentId + " to " + status.getExperimentState().toString());
        airavataRegistry.update(RegistryModelType.EXPERIMENT_STATUS, status, experimentId);
        return details.getExperimentStatus().getExperimentState();
    }

    public static boolean isFailedJob (JobExecutionContext jec) {
        JobStatus jobStatus = jec.getJobDetails().getJobStatus();
        if (jobStatus.getJobState() == JobState.FAILED) {
            return true;
        }
        return false;
    }

    public static boolean ackCancelRequest(String experimentId, ZooKeeper zk) throws KeeperException, InterruptedException {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentId, zk);
        String cancelNodePath = experimentEntry + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX;
        if (experimentEntry == null) {
            // This should be handle in validation request. Gfac shouldn't get any invalidate experiment.
            log.error("Cannot find the experiment Entry, so cancel operation cannot be performed. " +
                    "This happen when experiment completed and already removed from the zookeeper");
        } else {
            // check cancel operation is being processed for the same experiment.
            Stat cancelState = zk.exists(cancelNodePath, false);
            if (cancelState != null) {
                ZKUtil.deleteRecursive(zk,cancelNodePath);
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
