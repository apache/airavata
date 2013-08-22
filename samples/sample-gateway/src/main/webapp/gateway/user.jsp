<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %>
<%--
  Created by IntelliJ IDEA.
  User: thejaka
  Date: 7/31/13
  Time: 5:08 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    SampleGateway sampleGateway = null;
    sampleGateway = (SampleGateway)session.getAttribute(SampleGateway.GATEWAY_SESSION);

    if (sampleGateway == null) {
        sampleGateway = new SampleGateway(session.getServletContext());
    }

    session.setAttribute(SampleGateway.GATEWAY_SESSION, sampleGateway);

    String user = request.getParameter("username");
    String password = request.getParameter("password");

    boolean authenticate = sampleGateway.authenticate(user, password);

%>
<html>
<head>
    <title>Manage</title>
</head>
<body>
<h1>Sample Gateway</h1>

<%
    if (authenticate) {

        if (SampleGateway.isAdmin(user)) {
%>
<h1>Administration</h1>
<p>
    This page allows administration functionality.
<ol>
    <li><a href="acs.jsp">Retrieve Credentials</a></li>
    <li><a href="list_users.jsp">List Users</a></li>
</ol>
</p>


<%
     } else {
%>

<p> You are a normal user. You are not allowed to operate on this page.</p>

<%
     }
    } else {
%>

<h1>Authentication failed</h1>

<%
    }
%>

</body>
</html>