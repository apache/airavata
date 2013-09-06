<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %>
<%--
  Created by IntelliJ IDEA.
  User: thejaka
  Date: 8/5/13
  Time: 4:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

<%
    SampleGateway sampleGateway = (SampleGateway)session.getAttribute(SampleGateway.GATEWAY_SESSION);

    boolean success = false;

    String tokenId = request.getParameter("tokenId");

    if (tokenId != null) {
        sampleGateway.updateTokenId(tokenId);
        success = true;
    }
%>

<html>
<body>
<h2>Sample Gateway</h2>
<%
    out.println("The received token id - ");
    out.println(tokenId);

    if (success) {
%>
<p>Token id successfully updated.</p>

<p>
    View users who obtained token id.
<ol>
    <li><a href="list_users.jsp">List Users</a></li>
</ol>
</p>

<%
    } else {

%>
<p> Error updating token id.</p>
<%

    }

%>


</body>
</html>
