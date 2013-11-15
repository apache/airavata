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
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "wd.workflow_instanceID, wd.template_name, wd.status, wd.start_time," +
                    "wd.last_update_time, nd.node_id, nd.inputs, nd.outputs, " +
                    "e.project_name, e.submitted_date, nd.node_type, nd.status," +
                    "nd.start_time, nd.last_update_time " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "LEFT JOIN Workflow_Data wd " +
                    "ON e.experiment_ID = wd.experiment_ID " +
                    "LEFT JOIN Node_Data nd " +
                    "ON wd.workflow_instanceID = nd.workflow_instanceID " +
                    "WHERE e.experiment_ID ='" + experimentId + "'";


            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    if(experimentData == null){
                        experimentData = new ExperimentDataImpl();
                        experimentData.setExperimentId(rs.getString(1));
                        experimentData.setExperimentName(rs.getString(2));
                        experimentData.setUser(rs.getString(3));
                        experimentData.setMetadata(rs.getString(4));
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            String queryString = "SELECT ed.experiment_ID FROM Experiment_Data ed " +
                    "LEFT JOIN Experiment e " +
                    "ON ed.experiment_ID = e.experiment_ID " +
                    "WHERE ed.username ='" + user + "'";
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
            String queryString = "SELECT ed.name FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "WHERE e.experiment_ID='" + experimentId + "'";
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
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "wd.workflow_instanceID, wd.template_name, wd.status, wd.start_time," +
                    "wd.last_update_time, nd.node_id, nd.inputs, nd.outputs, " +
                    "e.project_name, e.submitted_date, nd.node_type, nd.status," +
                    "nd.start_time, nd.last_update_time" +
                    " FROM Experiment e INNER JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "LEFT JOIN Workflow_Data wd " +
                    "ON e.experiment_ID = wd.experiment_ID " +
                    "LEFT JOIN Node_Data nd " +
                    "ON wd.workflow_instanceID = nd.workflow_instanceID " +
                    "WHERE ed.username='" + user + "'";

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
                        experimentData.setMetadata(rs.getString(4));
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
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "wd.workflow_instanceID, wd.template_name, wd.status, wd.start_time," +
                    "wd.last_update_time, nd.node_id, nd.inputs, nd.outputs, " +
                    "e.project_name, e.submitted_date, nd.node_type, nd.status," +
                    "nd.start_time, nd.last_update_time" +
                    " FROM Experiment e INNER JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "LEFT JOIN Workflow_Data wd " +
                    "ON e.experiment_ID = wd.experiment_ID " +
                    "LEFT JOIN Node_Data nd " +
                    "ON wd.workflow_instanceID = nd.workflow_instanceID ";
            
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
            		if(to!=null && to!="") {
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
                        experimentData.setMetadata(rs.getString(4));
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
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "e.project_name, e.submitted_date " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "WHERE e.experiment_ID ='" + experimentId + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowExecution workflowInstance = new WorkflowExecution(experimentId, rs.getString(5));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowExecutionId(rs.getString(5));
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
            String queryString = "SELECT name FROM Experiment_Data WHERE name='" + experimentName + "'";
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
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "e.project_name, e.submitted_date " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "WHERE ed.username ='" + user + "'" +
                    " ORDER BY e.submitted_date ASC";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
                    experimentData.setMetadata(rs.getString(4));
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
