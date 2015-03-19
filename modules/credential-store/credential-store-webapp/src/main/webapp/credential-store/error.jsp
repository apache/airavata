<%@ page import="org.apache.airavata.credential.store.util.CredentialStoreConstants" %>
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
    String gatewayName = request.getParameter(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER);
    String portalUserName = request.getParameter(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER);
    Throwable exception = (Throwable) request.getAttribute("exception");

%>

<html>
<body>
<h1>Credential Store</h1>
<p>An error occurred while processing</p>
<p>
    Gateway Name - <%=gatewayName%>. Portal user name - <%=portalUserName%>.
    Exception -

</p>

<p>
    <%

        out.println("Exception - " + exception.getMessage());
        out.println();
        StackTraceElement[] elements = exception.getStackTrace();
        for (StackTraceElement element : elements) {
            out.print("         ");
            out.println(element.toString());
        }

    %>
</p>
</body>
</html>
