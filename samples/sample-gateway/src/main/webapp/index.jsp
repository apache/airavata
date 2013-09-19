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

<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %><%
    SampleGateway sampleGateway = new SampleGateway(session.getServletContext());
    session.setAttribute("Gateway", sampleGateway);
%>
<html>
<body>
<h2>Welcome to Sample Gateway</h2>

<form name="input" action="gateway/user.jsp" method="post">
    <input type="hidden" name="loginScreen" value="true">

    <table border="0" width="100%">
        <tr bgcolor="#999999">
            <td><font color="#f5f5f5">Login ...</font></td>
        </tr>
    </table>
    <table border="0" align="left">
        <tr>
            <td>User Name</td>
            <td><input type="text" name="username"></td>
        </tr>
        <tr>
            <td>Password</td>
            <td><input type="password" name="password"></td>
        </tr>
        <tr><td></td><td><input type="submit" value="Login"></td></tr>
    </table>
    <table border="0" width="100%">
        <tr bgcolor="#999999"><td>&nbsp;</td></tr>
    </table>
</form>

</body>
</html>
