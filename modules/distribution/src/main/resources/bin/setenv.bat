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

@echo off

:initialize
if "%AIRAVATA_HOME%"=="" set AIRAVATA_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET airavataDrive=%AIRAVATA_HOME:~0,1%
if not "%curDrive%" == "%airavataDrive%" %airavataDrive%:
goto updateClasspath

rem ----- update classpath -----------------------------------------------------
:updateClasspath
cd %AIRAVATA_HOME%
set AIRAVATA_CLASSPATH=
FOR %%C in ("%AIRAVATA_HOME%\lib\*.jar") DO set AIRAVATA_CLASSPATH=!AIRAVATA_CLASSPATH!;..\lib\%%~nC%%~xC

:end