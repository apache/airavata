<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %><%
    SampleGateway sampleGateway = new SampleGateway(session.getServletContext());
    session.setAttribute("Gateway", sampleGateway);
%>
<html>
<body>
<h2>Welcome to Sample Gateway</h2>

<form name="input" action="gateway/user.jsp" method="post">
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
