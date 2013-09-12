<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %>
<%
    SampleGateway sampleGateway = null;
    sampleGateway = (SampleGateway)session.getAttribute(SampleGateway.GATEWAY_SESSION);

    String user = (String) session.getAttribute("userName");

    String token = sampleGateway.getTokenIdForUser(user);
%>

<html>
<body>

<table width="100%" border="0">
    <tr bgcolor="#999999"><td align="right"><a href="user.jsp"><font color="#f5f5f5">Home</font> </a> <a href="logout.jsp"><font color="#f5f5f5">Logout</font></a></td></tr>
</table>

<h2>Sample Gateway</h2>

<p>Execute a Workflow.</p>

<img border="0" src="../images/echowf.png" alt="Echo Workflow">

<form name="executeJob" action="job_execute.jsp" method="post">

    <table border="0">
        <tr>
            <td><b>Configure Echo Workflow</b></td>
        </tr>

        <tr>
            <td>Host Name</td>
            <td><input type="text" name="hostName" size="65"></td>
        </tr>
        <tr>
            <td>Host Address</td>
            <td><input type="text" name="hostAddress" size="65"></td>
        </tr>
        <tr>
            <td>Gate Keeper Address</td>
            <td><input type="text" name="gateKeeperAddress" size="65"></td>
        </tr>
        <tr>
            <td>GRID FTP Endpoint</td>
            <td><input type="text" name="gridFTPEndpoint" size="65"></td>
        </tr>
        <tr>
            <td>Project Number</td>
            <td><input type="text" name="projectNumber" size="65"></td>
        </tr>
        <tr>
            <td>Queue Name</td>
            <td><input type="text" name="queueName" size="65"></td>
        </tr>
        <tr>
            <td>Working Directory</td>
            <td><input type="text" name="workingDirectory" size="65"></td>
        </tr>
        <tr>
            <td>Echo Input</td>
            <td><input type="text" name="echoInput" size="65"></td>
        </tr>
        <tr>
            <td>Associated Token Id</td>
            <td><%=token%></td>
        </tr>
    </table>

    <input type="submit" value="Configure & Run">
</form>

</body>
</html>