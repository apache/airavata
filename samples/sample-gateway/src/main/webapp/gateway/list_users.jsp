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
<%@ page import="java.util.List" %>
<%@ page import="org.apache.airavata.sample.gateway.userstore.User" %>
<%--
  Created by IntelliJ IDEA.
  User: thejaka
  Date: 8/5/13
  Time: 12:30 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    SampleGateway sampleGateway = (SampleGateway)session.getAttribute(SampleGateway.GATEWAY_SESSION);
%>

<html>
<head>
    <title>List Users</title>
</head>
<body>

<table width="100%" border="0">
    <tr bgcolor="#999999"><td align="right"><a href="user.jsp"><font color="#f5f5f5">Home</font> </a> <a href="logout.jsp"><font color="#f5f5f5">Logout</font></a></td></tr>
</table>

<h1>Sample Gateway</h1>


<p> This page lists all users and their attributes. </p>

<table>
    <tr>
        <td>UserName</td>
        <td>E-Mail</td>
        <td>TokenId</td>
    </tr>
<%
    List<User> userList = sampleGateway.getAllUsers();
    for (User u : userList) {
%>
    <tr>
        <td>
            <%=u.getUserName() %>
        </td>
        <td>
            <%=u.getEmail() %>
        </td>
        <td>
            <%=u.getToken() %>
        </td>

    </tr>
    <%
        }
    %>
</table>

</body>
</html>