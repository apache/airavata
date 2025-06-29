@echo off
rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements. See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership. The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License. You may obtain a copy of the License at
rem
rem http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied. See the License for the
rem specific language governing permissions and limitations
rem under the License.

setlocal EnableDelayedExpansion

call "%~dp0"setenv.bat

:loop
if ""%1""==""-xdebug"" goto xdebug
if ""%1""==""-security"" goto security
if ""%1""=="""" goto run
goto help

:xdebug
set JAVA_OPTS= %JAVA_OPTS% -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000
shift
goto loop

:security
set JAVA_OPTS=%JAVA_OPTS% -Djava.security.manager -Djava.security.policy=%AIRAVATA_HOME%\conf\axis2.policy -Daxis2.home=%AIRAVATA_HOME%
shift
goto loop

:help
echo  Usage: %0 [-options]
echo.
echo  where options include:
echo   -xdebug    Start Airavata Server under JPDA debugger
echo   -security  Enable Java 2 security
echo   -h         Help
goto end

:run
cd "%AIRAVATA_HOME%\bin"
set LOGO_FILE="logo.txt"
if exist "%LOGO_FILE%" type "%LOGO_FILE%"

java %JAVA_OPTS% -classpath "%AIRAVATA_CLASSPATH%" org.apache.airavata.server.ServerMain %*

:end
