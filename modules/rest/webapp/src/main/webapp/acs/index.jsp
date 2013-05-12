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
  
<html>
<body>
<h2>Sample Portal</h2>
<p>This demonstrates how portal can use Credential Store to obtain community credentials ...</p>
<form name="input" action="../credential-store" method="post">

    <table border="0">
        <tr>
            <td>Gateway Name</td>
            <td><input type="text" name="gatewayName"></td>
        </tr>
        <tr>
            <td>Portal Username</td>
            <td><input type="text" name="portalUserName"></td>
        </tr>
        <tr>
            <td>Contact Email</td>
            <td><input type="text" name="email"></td>
        </tr>
        <tr>
            <td>Associated Token</td>
            <td><input type="password" name="associatedToken"></td>
        </tr>
    </table>

    <input type="submit" value="Submit">
</form>
</body>
</html>
