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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


public class ExperimentDataRetriever {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentDataRetriever.class);

    public ExperimentData getExperiment(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<WorkflowExecution> experimentWorkflowInstances = new ArrayList<WorkflowExecution>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.EXPERIMENT_ID, e.EXPERIMENT_NAME, e.EXECUTION_USER, e.DESCRIPTION, " +
                    "wd.WORKFLOW_INSTANCE_ID, wd.TEMPLATE_NAME, wd.STATUS, wd.START_TIME," +
                    "wd.LAST_UPDATE_TIME, nd.NODE_ID, nd.INPUTS, nd.OUTPUTS, " +
                    "e.PROJECT_NAME, e.SUBMITTED_DATE, nd.NODE_TYPE, nd.STATUS," +
                    "nd.START_TIME, nd.LAST_UPDATE_TIME " +
                    "FROM EXPERIMENT_METADATA e " +
                    "LEFT JOIN WORKFLOW_DATA wd " +
                    "ON e.EXPERIMENT_ID = wd.EXPERIMENT_ID " +
                    "LEFT JOIN NODE_DATA nd " +
                    "ON wd.WORKFLOW_INSTANCE_ID = nd.WORKFLOW_INSTANCE_ID " +
                    "WHERE e.EXPERIMENT_ID ='" + experimentId + "'";


            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    if(experimentData == null){
                        experimentData = new ExperimentDataImpl();
                        experimentData.setExperimentId(rs.getString(1));
                        experimentData.setExperimentName(rs.getString(2));
                        experimentData.setUser(rs.getString(3));
//                        experimentData.setMetadata(rs.getString(4));
                        experimentData.setTopic(rs.getString(1));
                    }
                    fillWorkflowInstanceData(experimentData, rs, experimentWorkflowInstances);
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage());
        }catch (ParseException e) {
            logger.error(e.getMessage(), e);
        } catch (ExperimentLazyLoadedException e) {
            logger.error(e.getMessage());
        }
        return experimentData;
    }

    private void fillWorkflowInstanceData (ExperimentData experimentData,
                                                           ResultSet rs,
                                                           List<WorkflowExecution> workflowInstances) throws SQLException, ExperimentLazyLoadedException, ParseException {
        WorkflowExecutionDataImpl workflowInstanceData = (WorkflowExecutionDataImpl)experimentData.getWorkflowExecutionData(rs.getString(5));
        if (workflowInstanceData == null){
            WorkflowExecution workflowInstance = new WorkflowExecution(experimentData.getExperimentId(), rs.getString(5));
            workflowInstance.setTemplateName(rs.getString(6));
            workflowInstance.setExperimentId(rs.getString(1));
            workflowInstance.setWorkflowExecutionId(rs.getString(5));
            workflowInstances.add(workflowInstance);
            Date lastUpdateDate = getTime(rs.getString(9));
            String wdStatus = rs.getString(7);
            WorkflowExecutionStatus workflowExecutionStatus = new WorkflowExecutionStatus(workflowInstance,
                    createExecutionStatus(wdStatus),lastUpdateDate);
            workflowInstanceData = new WorkflowExecutionDataImpl(null,
                    workflowInstance, workflowExecutionStatus, null);
            ExperimentDataImpl expData = (ExperimentDataImpl) experimentData;
            workflowInstanceData.setExperimentData(expData);
            // Set the last updated workflow's status and time as the experiment's status
            if(expData.getExecutionStatus()!=null) {
            	if(expData.getExecutionStatus().getStatusUpdateTime().compareTo(workflowExecutionStatus.getStatusUpdateTime())<0) {
            		expData.setExecutionStatus(workflowExecutionStatus);
                }
            } else {
            	expData.setExecutionStatus(workflowExecutionStatus);
            }
            experimentData.getWorkflowExecutionDataList().add(workflowInstanceData);
        }
        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(workflowInstanceData.getWorkflowExecution(), rs.getString(10));
        NodeExecutionData workflowInstanceNodeData = new NodeExecutionData(workflowInstanceNode);

        String inputData = getStringValue(11, rs);
        String outputData = getStringValue(12, rs);

        workflowInstanceNodeData.setInput(inputData);
        workflowInstanceNodeData.setOutput(outputData);
        workflowInstanceNodeData.setStatus(createExecutionStatus(rs.getString(16)), getTime(rs.getString(18)));
        workflowInstanceNodeData.setType(WorkflowNodeType.getType(rs.getString(15)).getNodeType());
        workflowInstanceData.getNodeDataList().add(workflowInstanceNodeData);
    }

    private State createExecutionStatus (String status){
       return status == null ? State.UNKNOWN : State.valueOf(status);
    }

    private String getStringValue (int parameterNumber,  ResultSet rs) throws SQLException {
        Blob input = rs.getBlob(parameterNumber);
        if (input != null){
            byte[] inputBytes = input.getBytes(1, (int) input.length());
            String inputData = new String(inputBytes);
            return inputData;
        }
        return null;

    }

    private Date getTime (String date) throws ParseException {
        if (date != null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(date);
        }
        return null;

    }

    public List<String> getExperimentIdByUser(String user){
        List<String> result=new ArrayList<String>();
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement = null;
        try {
            String jdbcDriver =  Utils.getJDBCDriver();
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();

            // FIXME : pass user ID as a regular expression
            String queryString = "SELECT e.EXPERIMENT_ID FROM EXPERIMENT_METADATA e " +
                    "WHERE e.EXECUTION_USER ='" + user + "'";
            rs = statement.executeQuery(queryString);
            if(rs != null){
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage(), e);
        }

        return result;

    }

    public String getExperimentName(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection;
        Statement statement;
        ResultSet rs;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement =  connection.createStatement();
            String queryString = "SELECT e.name FROM EXPERIMENT_METADATA e " +
                    "WHERE e.EXPERIMENT_ID='" + experimentId + "'";
            rs = statement.executeQuery(queryString);
            if(rs != null){
                while (rs.next()) {
                    return rs.getString(1);
                }
            }
            if(rs != null){
                rs.close();
            }

            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public List<ExperimentData> getExperiments(String user) {
        String connectionURL = Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        Map<String, ExperimentData> experimentDataMap = new HashMap<String, ExperimentData>();
        List<ExperimentData> experimentDataList = new ArrayList<ExperimentData>();
        List<WorkflowExecution> experimentWorkflowInstances = new ArrayList<WorkflowExecution>();

        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(),
                    Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.EXPERIMENT_ID, e.EXPERIMENT_NAME, e.EXECUTION_USER, e.DESCRIPTION, " +
                    "wd.WORKFLOW_INSTANCE_ID, wd.TEMPLATE_NAME, wd.STATUS, wd.START_TIME," +
                    "wd.LAST_UPDATE_TIME, nd.NODE_ID, nd.INPUTS, nd.OUTPUTS, " +
                    "e.PROJECT_NAME, e.SUBMITTED_DATE, nd.NODE_TYPE, nd.STATUS, " +
                    "nd.START_TIME, nd.LAST_UPDATE_TIME " +
                    "FROM EXPERIMENT_METADATA e " +
                    "LEFT JOIN WORKFLOW_DATA wd " +
                    "ON e.EXPERIMENT_ID = wd.EXPERIMENT_ID " +
                    "LEFT JOIN NODE_DATA nd " +
                    "ON wd.WORKFLOW_INSTANCE_ID = nd.WORKFLOW_INSTANCE_ID " +
                    "WHERE e.EXECUTION_USER='" + user + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null) {
                while (rs.next()) {
                    ExperimentData experimentData = null;
                    if (experimentDataMap.containsKey(rs.getString(1))) {
                        experimentData = experimentDataMap.get(rs.getString(1));
                    }else{
                        experimentData = new ExperimentDataImpl();
                        experimentData.setExperimentId(rs.getString(1));
                        experimentData.setExperimentName(rs.getString(2));
                        experimentData.setUser(rs.getString(3));
//                        experimentData.setMetadata(rs.getString(4));
                        experimentData.setTopic(rs.getString(1));
                        experimentDataMap.put(experimentData.getExperimentId(),experimentData);
                        experimentDataList.add(experimentData);
                    }
                    fillWorkflowInstanceData(experimentData, rs, experimentWorkflowInstances);
                }
            }
            if (rs != null) {
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (ExperimentLazyLoadedException e) {
            logger.error(e.getMessage(), e);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
       return experimentDataList;

    }
    
    public List<ExperimentData> getExperiments(HashMap<String, String> params) {
    	String connectionURL = Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        Map<String, ExperimentData> experimentDataMap = new HashMap<String, ExperimentData>();
        List<ExperimentData> experimentDataList = new ArrayList<ExperimentData>();
        List<WorkflowExecution> experimentWorkflowInstances = new ArrayList<WorkflowExecution>();

        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(),
                    Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.EXPERIMENT_ID, e.EXPERIMENT_NAME, e.EXECUTION_USER, e.DESCRIPTION, " +
                    "wd.WORKFLOW_INSTANCE_ID, wd.TEMPLATE_NAME, wd.STATUS, wd.START_TIME," +
                    "wd.LAST_UPDATE_TIME, nd.NODE_ID, nd.INPUTS, nd.OUTPUTS, " +
                    "e.PROJECT_NAME, e.SUBMITTED_DATE, nd.NODE_TYPE, nd.STATUS, " +
                    "nd.START_TIME, nd.LAST_UPDATE_TIME " +
                    "FROM EXPERIMENT_METADATA e " +
                    "LEFT JOIN WORKFLOW_DATA wd " +
                    "ON e.EXPERIMENT_ID = wd.EXPERIMENT_ID " +
                    "LEFT JOIN NODE_DATA nd " +
                    "ON wd.WORKFLOW_INSTANCE_ID = nd.WORKFLOW_INSTANCE_ID ";
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(params.keySet().size()>0) {
            	queryString += "WHERE ";
            	String username = params.get("username");
            	String from = params.get("fromDate");
            	String to = params.get("toDate");
            	
            	if(username!=null && !username.isEmpty()) {
            		queryString += "ed.username='" + username + "'";
            		if((from!=null && !from.isEmpty()) || (to!=null && !to.isEmpty())) {
            			queryString += " AND ";
            		}
            	}
            	if(from!=null && !from.isEmpty()) {
            		Date fromDate = dateFormat.parse(from);
            		Timestamp fromTime = new Timestamp(fromDate.getTime());
            		queryString += "e.submitted_date>='" + fromTime + "'";
            		if(to!=null && !to.equals("")) {
            			queryString += " AND ";
            		}
            	}
            	if(to!=null && !to.isEmpty()) {
            		Date toDate = dateFormat.parse(to);
            		Timestamp toTime = new Timestamp(toDate.getTime());
            		queryString += "e.submitted_date<='" + toTime + "'";
            	}
            }
            rs = statement.executeQuery(queryString);
            if (rs != null) {
                while (rs.next()) {
                    ExperimentData experimentData = null;
                    if (experimentDataMap.containsKey(rs.getString(1))) {
                        experimentData = experimentDataMap.get(rs.getString(1));
                    }else{
                        experimentData = new ExperimentDataImpl();
                        experimentData.setExperimentId(rs.getString(1));
                        experimentData.setExperimentName(rs.getString(2));
                        experimentData.setUser(rs.getString(3));
//                        experimentData.setMetadata(rs.getString(4));
                        experimentData.setTopic(rs.getString(1));
                        experimentDataMap.put(experimentData.getExperimentId(),experimentData);
                        experimentDataList.add(experimentData);
                    }
                    fillWorkflowInstanceData(experimentData, rs, experimentWorkflowInstances);
                }
            }
            if (rs != null) {
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (ExperimentLazyLoadedException e) {
            logger.error(e.getMessage(), e);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
       return experimentDataList;
	}


    public ExperimentData getExperimentMetaInformation(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<WorkflowExecution> experimentWorkflowInstances = new ArrayList<WorkflowExecution>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.EXPERIMENT_ID, e.EXPERIMENT_NAME, e.EXECUTION_USER, " +
                    "e.PROJECT_NAME, e.SUBMITTED_DATE, wd.WORKFLOW_INSTANCE_ID " +
                    "FROM EXPERIMENT_METADATA e " +
                    "LEFT JOIN WORKFLOW_DATA wd " +
                    "ON e.EXPERIMENT_ID = wd.EXPERIMENT_ID " +
                    "WHERE e.EXPERIMENT_ID ='" + experimentId + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
//                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowExecution workflowInstance = new WorkflowExecution(experimentId, rs.getString(6));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowExecutionId(rs.getString(6));
                    experimentWorkflowInstances.add(workflowInstance);
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage(), e);
        }
        return experimentData;
    }

    public boolean isExperimentNameExist(String experimentName){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        try{
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT EXPERIMENT_NAME FROM EXPERIMENT_METADATA WHERE EXPERIMENT_NAME='" + experimentName + "'";
            rs = statement.executeQuery(queryString);
            if(rs != null){
                while (rs.next()) {
                    return true;
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    public List<ExperimentData> getAllExperimentMetaInformation(String user){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<ExperimentData> experimentDataList = new ArrayList<ExperimentData>();
        List<WorkflowExecution> experimentWorkflowInstances = new ArrayList<WorkflowExecution>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            //FIXME : pass user ID as a regular expression
            String queryString = "SELECT e.EXPERIMENT_ID, e.EXPERIMENT_NAME, e.EXECUTION_USER,  " +
                    "e.PROJECT_NAME, e.SUBMITTED_DATE " +
                    "FROM EXPERIMENT_METADATA e " +
                    "WHERE e.EXECUTION_USER ='" + user + "'" +
                    " ORDER BY e.SUBMITTED_DATE ASC";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
//                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowExecution workflowInstance = new WorkflowExecution(rs.getString(1), rs.getString(5));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowExecutionId(rs.getString(5));
                    experimentWorkflowInstances.add(workflowInstance);
                    experimentDataList.add(experimentData);
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage(), e);
        }
        return experimentDataList;
    }

	
}
