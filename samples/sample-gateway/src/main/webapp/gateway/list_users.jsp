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