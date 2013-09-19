<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %>
<%@ page import="org.apache.airavata.sample.gateway.ExecutionParameters" %>
<%@ page import="org.apache.airavata.sample.gateway.executor.WorkflowExecutor" %>
<%@ page import="org.apache.airavata.workflow.model.wf.Workflow" %>
<%@ page import="java.util.Arrays" %>
<%
    SampleGateway sampleGateway = null;
    sampleGateway = (SampleGateway)session.getAttribute(SampleGateway.GATEWAY_SESSION);

    String user = (String) session.getAttribute("userName");

    String token = sampleGateway.getTokenIdForUser(user);

    String hostName = request.getParameter("hostName");
    String hostAddress = request.getParameter("hostAddress");
    String gateKeeperAddress = request.getParameter("gateKeeperAddress");
    String gridFTPEndpoint = request.getParameter("gridFTPEndpoint");
    String projectNumber = request.getParameter("projectNumber");
    String queueName = request.getParameter("queueName");
    String workingDirectory = request.getParameter("workingDirectory");
    String echoInput = request.getParameter("echoInput");

    ExecutionParameters executionParameters = new ExecutionParameters();

    executionParameters.setHostAddress(hostAddress);
    executionParameters.setHostName(hostName);
    executionParameters.setGateKeeperAddress(gateKeeperAddress);
    executionParameters.setGridftpAddress(gridFTPEndpoint);
    executionParameters.setProjectNumber(projectNumber);
    executionParameters.setQueueName(queueName);
    executionParameters.setWorkingDirectory(workingDirectory);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor("default");

    String errorMessage = null;
    StackTraceElement[] stackTraceElements = null;
    String output = null;

    Workflow workflow = null;
    try {
        workflow = workflowExecutor.setupExperiment(executionParameters);
    } catch (Exception e) {

        e.printStackTrace();

        errorMessage = "An error occurred while setting up the experiment " + e.getMessage();
        stackTraceElements = e.getStackTrace();
    }

    if (errorMessage == null) {
        try {
            output = workflowExecutor.runWorkflow(workflow, Arrays.asList("echo_output=" + echoInput),
                    token, user);
        } catch (Exception e) {
            e.printStackTrace();

            errorMessage = "An error occurred while running the experiment " + e.getMessage();
            stackTraceElements = e.getStackTrace();
        }

    }

%>

<html>
<body>

<table width="100%" border="0">
    <tr bgcolor="#999999"><td align="right"><a href="user.jsp"><font color="#f5f5f5">Home</font> </a> <a href="logout.jsp"><font color="#f5f5f5">Logout</font></a></td></tr>
</table>

<h2>Sample Gateway</h2>

<p>Workflow Execution Results.</p>

<%
    if (errorMessage == null) {
%>
<p>Workflow successfully executed.</p>
<p>Output <%=output%> </p>

<%
    } else {
%>

<p><%=errorMessage%></p>
<p>Detail Error</p>
<p>
<%
    for (StackTraceElement stackTraceElement : stackTraceElements) {
%>
    <%=stackTraceElement.toString()%> <br>
<%
    }
    }
%>
</p>


</body>
</html>