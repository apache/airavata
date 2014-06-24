<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>

<%@ page import = "org.apache.airavata.credentialstore.local.LocalUserStore" %>
<%@ page import="org.apache.airavata.credentialstore.basic.BasicAccessAuthenticator" %>
<%@ page import="org.apache.airavata.credentialstore.session.HttpAuthenticatorFilter" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.airavata.common.utils.Constants" %>
<%

    LocalUserStore localUserStore = (LocalUserStore)session.getAttribute("LocalUserStore");

    if (localUserStore == null) {

        String operatingUser = (String) session.getAttribute(Constants.USER_IN_SESSION);

        if (operatingUser == null || !operatingUser.equals("admin")) {
            HttpAuthenticatorFilter.sendUnauthorisedError(response, "Insufficient privileges to perform user operations." +
                    " Only admin user is allowed to perform user operations.");

            return;
        }

        localUserStore = new LocalUserStore(application);

        session.setAttribute("LocalUserStore", localUserStore);
    }

    String operation = request.getParameter("operation");
    if (operation != null) {
        if (operation.equals("addUser")) {
            String userName = request.getParameter("username");
            String password = request.getParameter("newPassword");

            localUserStore.addUser(userName, password);
        } else if (operation.equals("deleteUser")) {
            String[] usersToDelete = request.getParameterValues("user-id");

            for (String deleteUser : usersToDelete) {
                localUserStore.deleteUser(deleteUser);
            }
        }
    }

    List<String> allUsers = localUserStore.getUsers();

%>

<html>
<head>
    <script language="javascript" type="text/javascript">

        function validate() {
            var checkSelected = false;
            for (var i = 0; i < <%=allUsers.size()%>; i++) {
                if (document.main["user-id"][i].checked) {
                    checkSelected = true;
                }
            }
            if (checkSelected) {
                var answer = confirm("Are you sure you want to delete selected users from the system ?");
                if (answer) {
                    return true;
                }
            } else {
                alert("Select at least one user to delete.");
            }
            return false;
        }

        function doProcess() {
            if (validate() == true) {
                document.main.submit();
            }
        }

    </script>
</head>
<body>
<img src="../images/airavata-logo-2.png">
<h2>Airavata REST API - Local User Store</h2>
<p><b>Manage Local User Store</b></p>


<form action="index.jsp" name="main" method="POST">
    <table>
        <tr>
            <td>&nbsp;</td>
            <td>All Users</td>
        </tr>
        <%
            for (String user : allUsers) {
        %>

        <tr>
            <td><input type="checkbox" name="user-id" value="<%=user%>"></td>
            <td><%=user%>
            </td>
            <td><a href="password.jsp?username=<%=user%>">Change Password</a></td>
        </tr>

        <%
            }
        %>
    </table>

    <br>

    <table width="100">
        <tr>
            <td>
                <a href="add.jsp"><input type="button" value="Add" name="Add"/></a>
            </td>
            <td>&nbsp;</td>
            <input type="hidden" name="operation" value="deleteUser">
            <td><input type="button" value="Delete" onclick="doProcess()"></td>
        </tr>
    </table>

</form>


</body>
</html>