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
  
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script type="text/javascript">
        <!--
        function redirect(){
            window.location = "${redirectUrl}"
        }
        //-->
    </script>
</head>
<body onLoad="setTimeout('redirect()', 1000)">
<h2>You will be now redirect to MyProxy portal !</h2>
<p>
    If your browser didn't redirect to MyProxy Portal within 1 minute click following link,
    <br><br> <a href="${redirectUrl}">${redirectUrl}</a>
</p>

</body>
</html>