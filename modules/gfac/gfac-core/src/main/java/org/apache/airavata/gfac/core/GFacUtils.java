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
package org.apache.airavata.gfac.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.watcher.CancelRequestWatcher;
import org.apache.airavata.gfac.core.watcher.RedeliveryRequestWatcher;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.commons.io.FileUtils;
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

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;

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

//	/**
//	 * This returns true if the give job is finished
//	 * otherwise false
//	 *
//	 * @param job
//	 * @return
//	 */
//	public static boolean isJobFinished(JobDescriptor job) {
//		if (org.apache.airavata.gfac.core.cluster.JobStatus.C.toString().equals(job.getStatus())) {
//			return true;
//		} else {
//			return false;
//		}
//	}

    /**
     * This will read
     *
     * @param maxWalltime
     * @return
     */
    public static String maxWallTimeCalculator(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime + ":00";
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes + ":00";
        }
    }

    public static String maxWallTimeCalculatorForLSF(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime;
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes;
        }
    }

//	public static boolean isSynchronousMode(
//			JobExecutionContext jobExecutionContext) {
//		GFacConfiguration gFacConfiguration = jobExecutionContext
//				.getGFacConfiguration();
//		if (ExecutionMode.ASYNCHRONOUS.equals(gFacConfiguration
//				.getExecutionMode())) {
//			return false;
//		}
//		return true;
//	}

    public static String readFileToString(String file)
            throws FileNotFoundException, IOException {
        BufferedReader instream = null;
        try {

            instream = new BufferedReader(new FileReader(file));
            StringBuffer buff = new StringBuffer();
            String temp = null;
            while ((temp = instream.readLine()) != null) {
                buff.append(temp);
                buff.append(GFacConstants.NEWLINE);
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
                || GFacConstants.LOCALHOST.equals(appHost) || GFacConstants._127_0_0_1
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

	public static void saveJobStatus(TaskContext taskContext,
                                     JobModel jobModel, JobState state) throws GFacException {
		try {
            // first we save job jobModel to the registry for sa and then save the job status.
            ProcessContext processContext = taskContext.getParentProcessContext();
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            JobStatus status = new JobStatus();
            status.setJobState(state);
            jobModel.setJobStatus(status);
            // FIXME - Should change according to the experiment catalog impl
//            experimentCatalog.add(ExpCatChildDataType.JOB_DETAIL, jobModel,
//                    new CompositeIdentifier(jobExecutionContext.getTaskData()
//                            .getTaskID(), jobModel.getJobID()));
            JobIdentifier identifier = new JobIdentifier(jobModel.getJobId(), taskContext.getTaskModel().getTaskId(),
                    processContext.getProcessId(), processContext.getProcessModel().getExperimentId(),
                    processContext.getGatewayId());
            JobStatusChangeRequestEvent jobStatusChangeRequestEvent = new JobStatusChangeRequestEvent(state, identifier);
            processContext.getLocalEventPublisher().publish(jobStatusChangeRequestEvent);
        } catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

    public static void saveTaskStatus(TaskContext taskContext,
                                      TaskState state) throws GFacException {
        try {
            // first we save job jobModel to the registry for sa and then save the job status.
            ProcessContext processContext = taskContext.getParentProcessContext();
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            TaskStatus status = new TaskStatus();
            status.setState(state);
            taskContext.getTaskModel().setTaskStatus(status);
            experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, status, taskContext.getTaskModel().getTaskId());
            TaskIdentifier identifier = new TaskIdentifier(taskContext.getTaskModel().getTaskId(),
                    processContext.getProcessId(), processContext.getProcessModel().getExperimentId(),
                    processContext.getGatewayId());
            TaskStatusChangeRequestEvent taskStatusChangeRequestEvent = new TaskStatusChangeRequestEvent(state, identifier);
            processContext.getLocalEventPublisher().publish(taskStatusChangeRequestEvent);
        } catch (Exception e) {
            throw new GFacException("Error persisting task status"
                    + e.getLocalizedMessage(), e);
        }
    }

    public static void saveProcessStatus(ProcessContext processContext,
                                      ProcessState state) throws GFacException {
        try {
            // first we save job jobModel to the registry for sa and then save the job status.
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            ProcessStatus status = new ProcessStatus();
            status.setState(state);
            processContext.getProcessModel().setProcessStatus(status);
            experimentCatalog.add(ExpCatChildDataType.PROCESS_STATUS, status, processContext.getProcessId());
            ProcessIdentifier identifier = new ProcessIdentifier(processContext.getProcessId(),
                                                                 processContext.getProcessModel().getExperimentId(),
                                                                 processContext.getGatewayId());
            ProcessStatusChangeRequestEvent processStatusChangeRequestEvent = new ProcessStatusChangeRequestEvent(state, identifier);
            processContext.getLocalEventPublisher().publish(processStatusChangeRequestEvent);
        } catch (Exception e) {
            throw new GFacException("Error persisting process status"
                    + e.getLocalizedMessage(), e);
        }
    }

    public static void saveExperimentStatus(ProcessContext processContext,
                                         ExperimentState state) throws GFacException {
        try {
            // first we save job jobModel to the registry for sa and then save the job status.
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            ExperimentStatus status = new ExperimentStatus();
            status.setState(state);

            experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_STATUS, status, processContext.getProcessModel().getExperimentId());
            ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent(state, processContext.getProcessModel().getExperimentId(), processContext.getGatewayId());
            processContext.getLocalEventPublisher().publish(experimentStatusChangeEvent);
        } catch (Exception e) {
            throw new GFacException("Error persisting experiment status"
                    + e.getLocalizedMessage(), e);
        }
    }

//	public static void updateJobStatus(JobExecutionContext jobExecutionContext,
//			JobDetails details, JobState state) throws GFacException {
//		try {
//			ExperimentCatalog experimentCatalog = jobExecutionContext.getExperimentCatalog();
//			JobStatus status = new JobStatus();
//			status.setJobState(state);
//			status.setTimeOfStateChange(Calendar.getInstance()
//					.getTimeInMillis());
//			details.setJobStatus(status);
//			experimentCatalog.update(
//					ExperimentCatalogModelType.JOB_DETAIL,
//					details, details.getJobID());
//		} catch (Exception e) {
//			throw new GFacException("Error persisting job status"
//					+ e.getLocalizedMessage(), e);
//		}
//	}

	public static void saveErrorDetails(
			ProcessContext processContext, String errorMessage)
			throws GFacException {
		try {
			ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
			ErrorModel details = new ErrorModel();
			details.setActualErrorMessage(errorMessage);
			details.setCreationTime(Calendar.getInstance().getTimeInMillis());
			// FIXME : Save error model according to new data model
//            experimentCatalog.add(ExpCatChildDataType.ERROR_DETAIL, details,
//					jobExecutionContext.getTaskData().getTaskID());
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

//	public static GfacExperimentState getZKExperimentState(CuratorFramework curatorClient,
//			JobExecutionContext jobExecutionContext)
//			throws Exception {
//		String expState = AiravataZKUtils.getExpState(curatorClient, jobExecutionContext
//				.getExperimentID());
//        if (expState == null || expState.isEmpty()) {
//            return GfacExperimentState.UNKNOWN;
//        }
//        return GfacExperimentState.findByValue(Integer.valueOf(expState));
//    }
//
//	public static boolean createHandlerZnode(CuratorFramework curatorClient,
//                                             JobExecutionContext jobExecutionContext, String className)
//			throws Exception {
//		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
//				jobExecutionContext.getExperimentID(), className);
//		Stat exists = curatorClient.checkExists().forPath(expState);
//		if (exists == null) {
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(expState, new byte[0]);
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
//		} else {
//			exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//			if (exists == null) {
//				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//						.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
//			}
//		}
//
//		exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//		if (exists != null) {
//			curatorClient.setData().withVersion(exists.getVersion())
//					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
//							String.valueOf(GfacHandlerState.INVOKING.getValue()).getBytes());
//		}
//		return true;
//	}

//	public static boolean createHandlerZnode(CuratorFramework curatorClient,
//                                             JobExecutionContext jobExecutionContext, String className,
//                                             GfacHandlerState state) throws Exception {
//		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
//				jobExecutionContext.getExperimentID(), className);
//		Stat exists = curatorClient.checkExists().forPath(expState);
//		if (exists == null) {
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//					.forPath(expState, new byte[0]);
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
//		} else {
//			exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//			if (exists == null) {
//				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//						.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0]);
//			}
//		}
//
//		exists = curatorClient.checkExists().forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//		if (exists != null) {
//			curatorClient.setData().withVersion(exists.getVersion())
//					.forPath(expState + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
//							String.valueOf(state.getValue()).getBytes());
//		}
//		return true;
//	}

//	public static boolean updateHandlerState(CuratorFramework curatorClient,
//                                             JobExecutionContext jobExecutionContext, String className,
//                                             GfacHandlerState state) throws Exception {
//		String handlerPath = AiravataZKUtils.getExpZnodeHandlerPath(
//				jobExecutionContext.getExperimentID(), className);
//		Stat exists = curatorClient.checkExists().forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//		if (exists != null) {
//			curatorClient.setData().withVersion(exists.getVersion())
//					.forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, String.valueOf(state.getValue()).getBytes());
//		} else {
//			createHandlerZnode(curatorClient, jobExecutionContext, className, state);
//		}
//		return false;
//	}

//	public static GfacHandlerState getHandlerState(CuratorFramework curatorClient,
//                                                  JobExecutionContext jobExecutionContext, String className) {
//		try {
//			String handlerPath = AiravataZKUtils.getExpZnodeHandlerPath( jobExecutionContext.getExperimentID(), className);
//			Stat exists = curatorClient.checkExists().forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
//			if (exists != null) {
//				String stateVal = new String(curatorClient.getData().storingStatIn(exists)
//						.forPath(handlerPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE));
//				return GfacHandlerState.findByValue(Integer.valueOf(stateVal));
//			}
//			return GfacHandlerState.UNKNOWN; // if the node doesn't exist or any other error we
//							// return false
//		} catch (Exception e) {
//			log.error("Error occured while getting zk node status", e);
//			return null;
//		}
//	}

//	// This method is dangerous because of moving the experiment data
//	public static boolean createExperimentEntryForPassive(String experimentID,
//														  String taskID, CuratorFramework curatorClient, String experimentNode,
//														  String pickedChild, String tokenId, long deliveryTag) throws Exception {
//		String experimentPath = experimentNode + File.separator + pickedChild;
//		String newExperimentPath = experimentPath + File.separator + experimentID;
//		Stat exists1 = curatorClient.checkExists().forPath(newExperimentPath);
//		String oldExperimentPath = GFacUtils.findExperimentEntry(experimentID, curatorClient);
//		if (oldExperimentPath == null) {  // this means this is a very new experiment
//			// are going to create a new node
//			log.info("This is a new Job, so creating all the experiment docs from the scratch");
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(newExperimentPath, new byte[0]);
//            String stateNodePath = curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//					.forPath(newExperimentPath + File.separator + "state",
//							String .valueOf(GfacExperimentState.LAUNCHED.getValue()) .getBytes());
//
//			if(curatorClient.checkExists().forPath(stateNodePath)!=null) {
//				log.info("Created the node: " + stateNodePath + " successfully !");
//			}else {
//				log.error("Error creating node: " + stateNodePath + " successfully !");
//			}
//			curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//					.forPath(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag));
//		} else {
//			log.error("ExperimentID: " + experimentID + " taskID: " + taskID + " was running by some Gfac instance,but it failed");
//            removeCancelDeliveryTagNode(oldExperimentPath, curatorClient); // remove previous cancel deliveryTagNode
//            if(newExperimentPath.equals(oldExperimentPath)){
//                log.info("Re-launch experiment came to the same GFac instance");
//            }else {
//				log.info("Re-launch experiment came to a new GFac instance so we are moving data to new gfac node");
//				curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE).forPath(newExperimentPath,
//						curatorClient.getData().storingStatIn(exists1).forPath(oldExperimentPath)); // recursively copy children
//                copyChildren(curatorClient, oldExperimentPath, newExperimentPath, 2); // we need to copy children up to depth 2
//				String oldDeliveryTag = oldExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX;
//				Stat exists = curatorClient.checkExists().forPath(oldDeliveryTag);
//				if(exists!=null) {
//					curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
//							.forPath(newExperimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX,
//									curatorClient.getData().storingStatIn(exists).forPath(oldDeliveryTag));
//					ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), oldDeliveryTag, true);
//				}
//				// After all the files are successfully transfered we delete the // old experiment,otherwise we do
//				// not delete a single file
//				log.info("After a successful copying of experiment data for an old experiment we delete the old data");
//				log.info("Deleting experiment data: " + oldExperimentPath);
//				ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), oldExperimentPath, true);
//			}
//		}
//		return true;
//	}

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
                copyChildren(curatorClient, oldChildPath, newChildPath, depth);
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
        String experimentNode = GFacConstants.ZOOKEEPER_EXPERIMENT_NODE;
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

    public static boolean setExperimentCancelRequest(String experimentId, CuratorFramework curatorClient, long
		    deliveryTag) throws Exception {
	    String experimentNode = ZKPaths.makePath(GFacConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
	    String cancelListenerNodePath = ZKPaths.makePath(experimentNode, GFacConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
	    curatorClient.setData().withVersion(-1).forPath(cancelListenerNodePath, GFacConstants.ZOOKEEPER_CANCEL_REQEUST
			    .getBytes());
	    return true;
    }

    public static boolean isCancelled(String experimentID, CuratorFramework curatorClient) throws Exception {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentID, curatorClient);
        if (experimentEntry == null) {
            return false;
        } else {
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

//    public static void saveHandlerData(JobExecutionContext jobExecutionContext,
//                                       StringBuffer data, String className) throws GFacHandlerException {
//		try {
//			CuratorFramework curatorClient = jobExecutionContext.getCuratorClient();
//			if (curatorClient != null) {
//				String expZnodeHandlerPath = AiravataZKUtils
//						.getExpZnodeHandlerPath(
//								jobExecutionContext.getExperimentID(),
//								className);
//				Stat exists = curatorClient.checkExists().forPath(expZnodeHandlerPath);
//                if (exists != null) {
//					curatorClient.setData().withVersion(exists.getVersion()).forPath(expZnodeHandlerPath, data.toString().getBytes());
//				} else {
//                    log.error("Saving Handler data failed, Stat is null");
//                }
//            }
//		} catch (Exception e) {
//			throw new GFacHandlerException(e);
//		}
//	}

//    public static String getHandlerData(ProcessContext processContext, String className) throws Exception {
//        CuratorFramework curatorClient = processContext.getCuratorClient();
//        if (curatorClient != null) {
//            String expZnodeHandlerPath = AiravataZKUtils
//                    .getExpZnodeHandlerPath(
//                            processContext.getExperimentID(),
//                            className);
//            Stat exists = curatorClient.checkExists().forPath(expZnodeHandlerPath);
//            return new String(processContext.getCuratorClient().getData().storingStatIn(exists).forPath(expZnodeHandlerPath));
//        }
//        return null;
//    }

    public static CredentialReader getCredentialReader()
            throws ApplicationSettingsException, IllegalAccessException,
            InstantiationException {
        try {
            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass,
                    driver));
        } catch (ClassNotFoundException e) {
            log.error("Not able to find driver: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    /**
     * To convert list to separated value
     *
     * @param listOfStrings
     * @param separator
     * @return
     */
    public static String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for (int i = 0; i < listOfStrings.size() - 1; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if (listOfStrings.size() > 0) {
            sb.append(listOfStrings.get(listOfStrings.size() - 1));
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
        ExperimentCatalog airavataExperimentCatalog = RegistryFactory.getDefaultExpCatalog();
        ExperimentModel details = (ExperimentModel) airavataExperimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
        if (details == null) {
            details = new ExperimentModel();
            details.setExperimentId(experimentId);
        }
        ExperimentStatus status = new ExperimentStatus();
        status.setState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        if (!ExperimentState.CANCELED.equals(details.getExperimentStatus().getState()) &&
                !ExperimentState.CANCELING.equals(details.getExperimentStatus().getState())) {
            status.setState(state);
        } else {
            status.setState(details.getExperimentStatus().getState());
        }
        details.setExperimentStatus(status);
        log.info("Updating the experiment status of experiment: " + experimentId + " to " + status.getState().toString());
        airavataExperimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT_STATUS, status, experimentId);
        return details.getExperimentStatus().getState();
    }

//    public static boolean isFailedJob(JobExecutionContext jec) {
////        JobStatus jobStatus = jec.getJobDetails().getJobStatus();
////        if (jobStatus.getJobState() == JobState.FAILED) {
////            return true;
////        }
//        return false;
//    }

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

//    public static void publishTaskStatus (JobExecutionContext jobExecutionContext, LocalEventPublisher publisher, TaskStatus state){
//        TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
//                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
//                jobExecutionContext.getExperimentID(),
//                jobExecutionContext.getGatewayID());
//        publisher.publish(new TaskStatusChangeRequestEvent(state, taskIdentity));
//    }

    public static String getZKGfacServersParentPath() {
        return ZKPaths.makePath(GFacConstants.ZOOKEEPER_SERVERS_NODE, GFacConstants.ZOOKEEPER_GFAC_SERVER_NODE);
    }

    public static JobDescriptor createJobDescriptor(ProcessContext processContext) throws GFacException, AppCatalogException, ApplicationSettingsException {
        JobDescriptor jobDescriptor = new JobDescriptor();
        String emailIds = null;
        ProcessModel processModel = processContext.getProcessModel();
        ResourceJobManager resourceJobManager = getResourceJobManager(processContext);
        if (isEmailBasedJobMonitor(processContext)) {
            emailIds = ServerSettings.getEmailBasedMonitorAddress();
        }
        if (ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_ENABLE).equalsIgnoreCase("true")) {
            String flags = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_FLAGS);
            if (flags != null && processContext.getComputeResourceDescription().getHostName().equals("stampede.tacc.xsede.org")) {
                flags = "ALL";
            }
            jobDescriptor.setMailOptions(flags);

            String userJobNotifEmailIds = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_EMAILIDS);
            if (userJobNotifEmailIds != null && !userJobNotifEmailIds.isEmpty()) {
                if (emailIds != null && !emailIds.isEmpty()) {
                    emailIds += ("," + userJobNotifEmailIds);
                } else {
                    emailIds = userJobNotifEmailIds;
                }
            }
            if (processModel.isEnableEmailNotification()) {
                List<String> emailList = processModel.getEmailAddresses();
                String elist = GFacUtils.listToCsv(emailList, ',');
                if (elist != null && !elist.isEmpty()) {
                    if (emailIds != null && !emailIds.isEmpty()) {
                        emailIds = emailIds + "," + elist;
                    } else {
                        emailIds = elist;
                    }
                }
            }
        }
        if (emailIds != null && !emailIds.isEmpty()) {
            log.info("Email list: " + emailIds);
            jobDescriptor.setMailAddress(emailIds);
        }

        jobDescriptor.setInputDirectory(processContext.getInputDir());
        jobDescriptor.setOutputDirectory(processContext.getOutputDir());
        jobDescriptor.setExecutablePath(processContext.getApplicationDeploymentDescription().getExecutablePath());
        jobDescriptor.setStandardOutFile(processContext.getStdoutLocation());
        jobDescriptor.setStandardErrorFile(processContext.getStderrLocation());
        String computationalProjectAccount = getComputeResourcePreference(processContext).getAllocationProjectNumber();
        if (computationalProjectAccount != null) {
            jobDescriptor.setAcountString(computationalProjectAccount);
        }
        // To make job name alpha numeric
        jobDescriptor.setJobName("A" + String.valueOf(generateJobName()));
        jobDescriptor.setWorkingDirectory(processContext.getWorkingDir());

        List<String> inputValues = new ArrayList<String>();
        List<InputDataObjectType> processInputs = processModel.getProcessInputs();

        // sort the inputs first and then build the command ListR
        Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
            @Override
            public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                return inputDataObjectType.getInputOrder() - t1.getInputOrder();
            }
        };
        Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
        for (InputDataObjectType input : processInputs) {
            sortedInputSet.add(input);
        }
        for (InputDataObjectType inputDataObjectType : sortedInputSet) {
            if (!inputDataObjectType.isRequiredToAddedToCommandLine()) {
                continue;
            }
            if (inputDataObjectType.getApplicationArgument() != null
                    && !inputDataObjectType.getApplicationArgument().equals("")) {
                inputValues.add(inputDataObjectType.getApplicationArgument());
            }

            if (inputDataObjectType.getValue() != null
                    && !inputDataObjectType.getValue().equals("")) {
                if (inputDataObjectType.getType() == DataType.URI) {
                    // set only the relative path
                    String filePath = inputDataObjectType.getValue();
                    filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                    inputValues.add(filePath);
                } else {
                    inputValues.add(inputDataObjectType.getValue());
                }

            }
        }

        List<OutputDataObjectType> processOutputs = processModel.getProcessOutputs();
        for (OutputDataObjectType output : processOutputs) {
            if (output.getApplicationArgument() != null
                    && !output.getApplicationArgument().equals("")) {
                inputValues.add(output.getApplicationArgument());
            }
            if (output.getValue() != null && !output.getValue().equals("") && output.isRequiredToAddedToCommandLine()) {
                if (output.getType() == DataType.URI) {
                    String filePath = output.getValue();
                    filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                    inputValues.add(filePath);
                }
            }
        }

        jobDescriptor.setInputValues(inputValues);
        jobDescriptor.setUserName(processContext.getRemoteCluster().getServerInfo().getUserName());
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setOwner(processContext.getRemoteCluster().getServerInfo().getUserName());

        ComputationalResourceSchedulingModel scheduling = processModel.getResourceSchedule();
        if (scheduling != null) {
            int totalNodeCount = scheduling.getNodeCount();
            int totalCPUCount = scheduling.getTotalCPUCount();

            if (scheduling.getQueueName() != null) {
                jobDescriptor.setQueueName(scheduling.getQueueName());
            }

            if (totalNodeCount > 0) {
                jobDescriptor.setNodes(totalNodeCount);
            }

            if (scheduling.getQueueName() != null) {
                jobDescriptor.setQueueName(scheduling.getQueueName());
            }
            if (totalCPUCount > 0) {
                int ppn = totalCPUCount / totalNodeCount;
                jobDescriptor.setProcessesPerNode(ppn);
                jobDescriptor.setCPUCount(totalCPUCount);
            }
            if (scheduling.getWallTimeLimit() > 0) {
                jobDescriptor.setMaxWallTime(String.valueOf(scheduling.getWallTimeLimit()));
                if (resourceJobManager != null) {
                    if (resourceJobManager.getResourceJobManagerType().equals(ResourceJobManagerType.LSF)) {
                        jobDescriptor.setMaxWallTimeForLSF(String.valueOf(scheduling.getWallTimeLimit()));
                    }
                }
            }
            if (scheduling.getTotalPhysicalMemory() > 0) {
                jobDescriptor.setUsedMemory(scheduling.getTotalPhysicalMemory() + "");
            }
        } else {
            log.error("Task scheduling cannot be null at this point..");
        }
        ApplicationDeploymentDescription appDepDescription = processContext.getApplicationDeploymentDescription();
        List<String> moduleCmds = appDepDescription.getModuleLoadCmds();
        if (moduleCmds != null) {
            for (String moduleCmd : moduleCmds) {
                jobDescriptor.addModuleLoadCommands(moduleCmd);
            }
        }
        List<String> preJobCommands = appDepDescription.getPreJobCommands();
        if (preJobCommands != null) {
            for (String preJobCommand : preJobCommands) {
                jobDescriptor.addPreJobCommand(parseCommand(preJobCommand, processContext));
            }
        }

        List<String> postJobCommands = appDepDescription.getPostJobCommands();
        if (postJobCommands != null) {
            for (String postJobCommand : postJobCommands) {
                jobDescriptor.addPostJobCommand(parseCommand(postJobCommand, processContext));
            }
        }

        ApplicationParallelismType parallelism = appDepDescription.getParallelism();
        if (parallelism != null) {
            if (parallelism == ApplicationParallelismType.MPI || parallelism == ApplicationParallelismType.OPENMP || parallelism == ApplicationParallelismType.OPENMP_MPI) {
                if (resourceJobManager != null) {
                    Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
                    if (jobManagerCommands != null && !jobManagerCommands.isEmpty()) {
                        for (JobManagerCommand command : jobManagerCommands.keySet()) {
                            if (command == JobManagerCommand.SUBMISSION) {
                                String commandVal = jobManagerCommands.get(command);
                                jobDescriptor.setJobSubmitter(commandVal);
                            }
                        }
                    }
                }
            }
        }
        return jobDescriptor;
    }

    private static int generateJobName() {
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE);
        i = i + 99999999;
        if (i < 0) {
            i = i * (-1);
        }
        return i;
    }

    private static String parseCommand(String value, ProcessContext context) {
        String parsedValue = value.replaceAll("\\$workingDir", context.getWorkingDir());
        parsedValue = parsedValue.replaceAll("\\$inputDir", context.getInputDir());
        parsedValue = parsedValue.replaceAll("\\$outputDir", context.getOutputDir());
        return parsedValue;
    }

    public static ResourceJobManager getResourceJobManager(ProcessContext processContext) {
        try {
            JobSubmissionProtocol submissionProtocol = getPreferredJobSubmissionProtocol(processContext);
            JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processContext);
            if (submissionProtocol == JobSubmissionProtocol.SSH) {
                SSHJobSubmission sshJobSubmission = GFacUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getResourceJobManager();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission = GFacUtils.getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getResourceJobManager();
                }
            }
        } catch (AppCatalogException e) {
            log.error("Error occured while retrieving resource job manager", e);
        }
        return null;
    }

    public static boolean isEmailBasedJobMonitor(ProcessContext processContext) throws GFacException, AppCatalogException {
        JobSubmissionProtocol jobSubmissionProtocol = getPreferredJobSubmissionProtocol(processContext);
        JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processContext);
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH) {
            String jobSubmissionInterfaceId = jobSubmissionInterface.getJobSubmissionInterfaceId();
            SSHJobSubmission sshJobSubmission = processContext.getAppCatalog().getComputeResource().getSSHJobSubmission(jobSubmissionInterfaceId);
            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
            return monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR;
        } else {
            return false;
        }
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(ProcessContext context) throws AppCatalogException {
        try {
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            ComputeResourceDescription resourceDescription = context.getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
            List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
            if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                Collections.sort(jobSubmissionInterfaces, new Comparator<JobSubmissionInterface>() {
                    @Override
                    public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                        return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                    }
                });
            } else {
                throw new AppCatalogException("Compute resource should have at least one job submission interface defined...");
            }
            return jobSubmissionInterfaces.get(0);
        } catch (AppCatalogException e) {
            throw new AppCatalogException("Error occurred while retrieving data from app catalog", e);
        }
    }

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(ProcessContext context) throws AppCatalogException {
        try {
            GwyResourceProfile gatewayProfile = context.getAppCatalog().getGatewayProfile();
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            ComputeResourcePreference preference = gatewayProfile.getComputeResourcePreference(context.getGatewayId(), resourceHostId);
            return preference.getPreferredJobSubmissionProtocol();
        } catch (AppCatalogException e) {
            log.error("Error occurred while initializing app catalog", e);
            throw new AppCatalogException("Error occurred while initializing app catalog", e);
        }
    }

    public static ComputeResourcePreference getComputeResourcePreference(ProcessContext context) throws AppCatalogException {
        try {
            GwyResourceProfile gatewayProfile = context.getAppCatalog().getGatewayProfile();
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            return gatewayProfile.getComputeResourcePreference(context.getGatewayId(), resourceHostId);
        } catch (AppCatalogException e) {
            log.error("Error occurred while initializing app catalog", e);
            throw new AppCatalogException("Error occurred while initializing app catalog", e);
        }
    }

    public static File createJobFile(JobDescriptor jobDescriptor, JobManagerConfiguration jobManagerConfiguration) throws GFacException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            URL resource = GFacUtils.class.getClassLoader().getResource(jobManagerConfiguration.getJobDescriptionTemplateName());

            if (resource == null) {
                String error = "System configuration file '" + jobManagerConfiguration.getJobDescriptionTemplateName()
                        + "' not found in the classpath";
                throw new GFacException(error);
            }

            Source xslt = new StreamSource(new File(resource.getPath()));
            Transformer transformer;
            StringWriter results = new StringWriter();
            File tempPBSFile = null;
            // generate the pbs script using xslt
            transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new ByteArrayInputStream(jobDescriptor.toXML().getBytes()));
            transformer.transform(text, new StreamResult(results));
            String scriptContent = results.toString().replaceAll("^[ |\t]*\n$", "");
            if (scriptContent.startsWith("\n")) {
                scriptContent = scriptContent.substring(1);
            }
            // creating a temporary file using pbs script generated above
            int number = new SecureRandom().nextInt();
            number = (number < 0 ? -number : number);
            tempPBSFile = new File(Integer.toString(number) + jobManagerConfiguration.getScriptExtension());
            FileUtils.writeStringToFile(tempPBSFile, scriptContent);
            return tempPBSFile;
        } catch (IOException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        } catch (TransformerConfigurationException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        } catch (TransformerException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        }
    }

	public static String getExperimentNodePath(String experimentId) {
		return GFacConstants.ZOOKEEPER_EXPERIMENT_NODE + File.separator + experimentId;
	}

	public static void createExperimentNode(CuratorFramework curatorClient, String gfacServerName, String
			experimentId, long deliveryTag, String token) throws Exception {
		// create /experiments/experimentId node and set data - serverName, add redelivery listener
		String experimentPath = ZKPaths.makePath(GFacConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentPath);
		curatorClient.setData().withVersion(-1).forPath(experimentPath, gfacServerName.getBytes());
		curatorClient.getData().usingWatcher(new RedeliveryRequestWatcher()).forPath(experimentPath);

		// create /experiments/experimentId/deliveryTag node and set data - deliveryTag
		String deliveryTagPath = ZKPaths.makePath(experimentPath, GFacConstants.ZOOKEEPER_DELIVERYTAG_NODE);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), deliveryTagPath);
		curatorClient.setData().withVersion(-1).forPath(deliveryTagPath, GFacUtils.longToBytes(deliveryTag));

		// create /experiments/experimentId/token node and set data - token
		String tokenNodePath = ZKPaths.makePath(experimentId, GFacConstants.ZOOKEEPER_TOKEN_NODE);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), tokenNodePath);
		curatorClient.setData().withVersion(-1).forPath(tokenNodePath, token.getBytes());

		// create /experiments/experimentId/cancelListener node and set watcher for data changes
		String cancelListenerNode = ZKPaths.makePath(experimentPath, GFacConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), cancelListenerNode);
		curatorClient.getData().usingWatcher(new CancelRequestWatcher()).forPath(cancelListenerNode);
	}
}
