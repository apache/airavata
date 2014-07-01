/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.app.catalog.test;


import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ComputeResource;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogImpl;
import org.apache.airavata.app.catalog.test.util.Initialize;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.computehost.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class ComputeResourceTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;

    @Before
    public void setUp() {
        try {
            AiravataUtils.setExecutionAsServer();
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            appcatalog = AppCatalogFactory.getAppCatalog();
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        initialize.stopDerbyServer();
    }

    @Test
    public void testAddComputeResource (){
        try {
            ComputeResource computeResource = appcatalog.getComputeResource();
            ComputeResourceDescription description = new ComputeResourceDescription();

            description.setHostName("localhost");
            description.setPreferredJobSubmissionProtocol("SSH");
            description.setResourceDescription("test compute resource");
            Set<String> ipdaresses = new HashSet<String>();
            ipdaresses.add("222.33.43.444");
            ipdaresses.add("23.344.44.454");
            description.setIpAddresses(ipdaresses);
            Set<String> aliases = new HashSet<String>();
            aliases.add("test.alias1");
            aliases.add("test.alias2");
            description.setHostAliases(aliases);
            String sshsubmissionId = addSSHJobSubmission();
            System.out.println("**** SSH Submission id ****** :" + sshsubmissionId);
            String gsiSSHsubmissionId = addGSISSHJobSubmission();
            System.out.println("**** GSISSH Submission id ****** :" + gsiSSHsubmissionId);
            String globusSubmissionId = addGlobusJobSubmission();
            System.out.println("**** Globus Submission id ****** :" + globusSubmissionId);
            Map<String, JobSubmissionProtocol> jobProtools = new HashMap<String, JobSubmissionProtocol>();
            jobProtools.put(sshsubmissionId, JobSubmissionProtocol.SSH);
            jobProtools.put(gsiSSHsubmissionId, JobSubmissionProtocol.GSISSH);
            jobProtools.put(globusSubmissionId, JobSubmissionProtocol.GRAM);
            description.setJobSubmissionProtocols(jobProtools);

            String scpDataMoveId = addSCPDataMovement();
            System.out.println("**** SCP DataMoveId****** :" + scpDataMoveId);
            String gridFTPDataMoveId = addGridFTPDataMovement();
            System.out.println("**** grid FTP DataMoveId****** :" + gridFTPDataMoveId);
            Map<String, DataMovementProtocol> dataProtools = new HashMap<String, DataMovementProtocol>();
            dataProtools.put(scpDataMoveId, DataMovementProtocol.SCP);
            dataProtools.put(gridFTPDataMoveId, DataMovementProtocol.GridFTP);
            description.setDataMovementProtocols(dataProtools);

            String resourceId = computeResource.addComputeResource(description);
            System.out.println("**********Resource id ************* : " +  resourceId);
            ComputeResourceDescription host = null;
            if (computeResource.isComputeResourceExists(resourceId)){
                host = computeResource.getComputeResource(resourceId);
                System.out.println("**********Resource name ************* : " +  host.getHostName());
            }

            SSHJobSubmission sshJobSubmission = computeResource.getSSHJobSubmission(sshsubmissionId);
            System.out.println("**********SSH Submission resource job manager ************* : " +  sshJobSubmission.getResourceJobManager().toString());

            GSISSHJobSubmission gsisshJobSubmission = computeResource.getGSISSHJobSubmission(gsiSSHsubmissionId);
            System.out.println("**********GSISSH Submission resource job manager ************* : " +  gsisshJobSubmission.getResourceJobManager().toString());

            GlobusJobSubmission globusJobSubmission = computeResource.getGlobusJobSubmission(globusSubmissionId);
            System.out.println("**********Globus Submission resource job manager ************* : " + globusJobSubmission.getResourceJobManager().toString());

            SCPDataMovement scpDataMovement = computeResource.getSCPDataMovement(scpDataMoveId);
            System.out.println("**********SCP Data Move Security protocol ************* : " + scpDataMovement.getSecurityProtocol().toString());

            GridFTPDataMovement gridFTPDataMovement = computeResource.getGridFTPDataMovement(gridFTPDataMoveId);
            System.out.println("**********GRID FTP Data Move Security protocol ************* : " + gridFTPDataMovement.getSecurityProtocol().toString());
            assertTrue("Compute resource save successfully", host != null);
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }
    }

    public String addSSHJobSubmission (){
        try {
            SSHJobSubmission jobSubmission = new SSHJobSubmission();
            jobSubmission.setSshPort(22);
            jobSubmission.setResourceJobManager(ResourceJobManager.PBS);
            return appcatalog.getComputeResource().addSSHJobSubmission(jobSubmission);
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addGSISSHJobSubmission (){
        try {
            GSISSHJobSubmission jobSubmission = new GSISSHJobSubmission();
            jobSubmission.setSshPort(22);
            jobSubmission.setResourceJobManager(ResourceJobManager.PBS);
            Set<String> exports = new HashSet<String>();
            exports.add("export1");
            exports.add("export2");
            jobSubmission.setExports(exports);
            jobSubmission.setMonitorMode("testMonitormode");
            List<String> preJobCommands = new ArrayList<String>();
            preJobCommands.add("prejobcommand1");
            preJobCommands.add("prejobcommand2");
            jobSubmission.setPreJobCommands(preJobCommands);
            List<String> postJobCommands = new ArrayList<String>();
            postJobCommands.add("postjobcommand1");
            postJobCommands.add("postjobcommand2");
            jobSubmission.setPostJobCommands(postJobCommands);
            jobSubmission.setInstalledPath("installedPath");
            return appcatalog.getComputeResource().addGSISSHJobSubmission(jobSubmission);
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addGlobusJobSubmission (){
        try {
            GlobusJobSubmission jobSubmission = new GlobusJobSubmission();
            jobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
            jobSubmission.setResourceJobManager(ResourceJobManager.PBS);
            List<String> endPoints = new ArrayList<String>();
            endPoints.add("222.33.43.444");
            endPoints.add("23.344.44.454");
            jobSubmission.setGlobusGateKeeperEndPoint(endPoints);
            return appcatalog.getComputeResource().addGlobusJobSubmission(jobSubmission);
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addSCPDataMovement (){
        try {
            SCPDataMovement dataMovement = new SCPDataMovement();
            dataMovement.setSshPort(22);
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            return appcatalog.getComputeResource().addScpDataMovement(dataMovement);
        }catch (AppCatalogException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addGridFTPDataMovement (){
        try {
            GridFTPDataMovement dataMovement = new GridFTPDataMovement();
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            List<String> endPoints = new ArrayList<String>();
            endPoints.add("222.33.43.444");
            endPoints.add("23.344.44.454");
            dataMovement.setGridFTPEndPoint(endPoints);
            return appcatalog.getComputeResource().addGridFTPDataMovement(dataMovement);
        }catch (AppCatalogException e) {
            e.printStackTrace();
        }
        return null;
    }


}

