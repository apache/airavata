WS-Messenger 0.0.1-SNAPSHOT
---------------




New Features In This Release
----------------------------



Key Features
------------


Issues Fixed in This Release
----------------------------


Installation & Running
----------------------
1. extract the downloaded zip file
2. Run the axis2server.sh or axis2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:8280/axis2/services
4. You can see the deployed services in simple Axis2 server, run the samples in client-api/samples directory.

For more details, see the Installation Guide

System Requirements
-------------------



Known Issues in This Release
----------------------------



Including External Dependencies
--------------------------------


WS-Messenger Binary Distribution Directory Structure
--------------------------------------------

    WS-MESSENGER_HOME
        |-- gui <folder>
        |-- standalone-server <folder>
        |   |-- lib <folder>
        |   |-- conf <folder>
        |   |-- repository <folder>
        |       -- services <folder>
        |   |-- bin <folder>
                |-- database_scripts
        |-- client-api <folder>
        |   |-- lib <folder>
        |   |-- samples <folder>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>



    - gui
      Contains the scripts to run WS Notification Listener GUI tool

    - standalone-server
      This contains all the artifacts required during axis2Server runtime with required axis2 service archives

    - standalone-server - lib
      Contains all the libraries required for Axis2 runtime

    - standalone-server - conf
      Contains all the configuration files for Axis2 Rutime and messenger services (messagebroker and messagebox)

    - standalone-server - repository - services
      Contains deployed services in Axis2 runtime.

    - standalone-server - bin
      Commandline scripts to use to start the SimpleAxis2Server and other Axis2 related scripts.

    - standalone-server - bin - database_scripts
      Contains the database scripts which are used to create tables for messagebox and messagebroker services

    - client-api
      Contains all the client side artifacts, which contains client samples and required libraries.

    - client-api - lib
      Required libraries for client side usage.

    - client-api - samples
      Contains set of samples which demostrate the funcationality of messagebroker and messagebox.

    - README.txt
      This document.

    - INSTALL.txt
          This document will contain information on installing Airavata-WS-Messenger.



