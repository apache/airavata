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
package org.apache.airavata.gfac.provider.utils;

import java.io.File;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.ExtendedKeyValueType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.ogf.schemas.jsdl.ApplicationType;
import org.ogf.schemas.jsdl.JobDefinitionType;
import org.ogf.schemas.jsdl.posix.EnvironmentType;
import org.ogf.schemas.jsdl.posix.FileNameType;
import org.ogf.schemas.jsdl.spmd.NumberOfProcessesType;
import org.ogf.schemas.jsdl.spmd.ProcessesPerHostType;
import org.ogf.schemas.jsdl.spmd.ThreadsPerProcessType;

public class ApplicationProcessor {

    public static void generateJobSpecificAppElements(JobDefinitionType value, JobExecutionContext context) {

        HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context.getApplicationContext()
                .getApplicationDeploymentDescription().getType();

        createGenericApplication(value, appDepType);

        if (appDepType.getApplicationEnvironmentArray().length > 0) {
            createApplicationEnvironment(value, appDepType.getApplicationEnvironmentArray(), appDepType);
        }

        if (appDepType.getExecutableLocation() != null) {
            FileNameType fNameType = FileNameType.Factory.newInstance();
            fNameType.setStringValue(appDepType.getExecutableLocation());
            if (isParallelJob(appDepType)) {
                JSDLUtils.getOrCreateSPMDApplication(value).setExecutable(fNameType);
                JSDLUtils.getSPMDApplication(value).setSPMDVariation(getSPMDVariation(appDepType));

                if (getValueFromMap(appDepType, JSDLUtils.NUMBEROFPROCESSES) != null) {
                    NumberOfProcessesType num = NumberOfProcessesType.Factory.newInstance();
                    num.setStringValue(getValueFromMap(appDepType, JSDLUtils.NUMBEROFPROCESSES));
                    JSDLUtils.getSPMDApplication(value).setNumberOfProcesses(num);
                }

                if (getValueFromMap(appDepType, JSDLUtils.PROCESSESPERHOST) != null) {
                    ProcessesPerHostType pph = ProcessesPerHostType.Factory.newInstance();
                    pph.setStringValue(getValueFromMap(appDepType, JSDLUtils.PROCESSESPERHOST));
                    JSDLUtils.getSPMDApplication(value).setProcessesPerHost(pph);
                }

                if (getValueFromMap(appDepType, JSDLUtils.THREADSPERHOST) != null) {
                    ThreadsPerProcessType tpp = ThreadsPerProcessType.Factory.newInstance();
                    tpp.setStringValue(getValueFromMap(appDepType, JSDLUtils.THREADSPERHOST));
                    JSDLUtils.getSPMDApplication(value).setThreadsPerProcess(tpp);
                }
            } else
                JSDLUtils.getOrCreatePOSIXApplication(value).setExecutable(fNameType);
        }

        if (appDepType.getStandardOutput() != null) {
            String stdout = new File(appDepType.getStandardOutput()).getName();
            ApplicationProcessor.setApplicationStdOut(value, appDepType, stdout);
        }

        if (appDepType.getStandardError() != null) {
            String stderr = new File(appDepType.getStandardError()).getName();
            ApplicationProcessor.setApplicationStdErr(value, appDepType, stderr);
        }

    }

    public static boolean isParallelJob(HpcApplicationDeploymentType appDepType) {

        boolean isParallel = false;

        if (appDepType.getJobType() != null) {
            // TODO set data output directory
            int status = appDepType.getJobType().intValue();

            switch (status) {
            // TODO: this check should be done outside this class
            case JobTypeType.INT_MPI:
            case JobTypeType.INT_OPEN_MP:
                isParallel = true;
                break;

            case JobTypeType.INT_SERIAL:
            case JobTypeType.INT_SINGLE:
                isParallel = false;
                break;

            default:
                isParallel = false;
                break;
            }
        }
        return isParallel;
    }

    public static void createApplicationEnvironment(JobDefinitionType value, NameValuePairType[] nameValuePairs,
            HpcApplicationDeploymentType appDepType) {

        if (isParallelJob(appDepType)) {
            for (NameValuePairType nv : nameValuePairs) {
                EnvironmentType envType = JSDLUtils.getOrCreateSPMDApplication(value).addNewEnvironment();
                envType.setName(nv.getName());
                envType.setStringValue(nv.getValue());
            }
        } else {
            for (NameValuePairType nv : nameValuePairs) {
                EnvironmentType envType = JSDLUtils.getOrCreatePOSIXApplication(value).addNewEnvironment();
                envType.setName(nv.getName());
                envType.setStringValue(nv.getValue());
            }
        }

    }

    public static String getSPMDVariation(HpcApplicationDeploymentType appDepType) {

        String variation = null;

        if (appDepType.getJobType() != null) {
            // TODO set data output directory
            int status = appDepType.getJobType().intValue();

            switch (status) {
            // TODO: this check should be done outside this class
            case JobTypeType.INT_MPI:
                variation = SPMDVariations.MPI.value();
                break;

            case JobTypeType.INT_OPEN_MP:
                variation = SPMDVariations.OpenMPI.value();
                break;

            }
        }
        return variation;
    }

    public static void addApplicationArgument(JobDefinitionType value, HpcApplicationDeploymentType appDepType,
            String stringPrm) {
        if (isParallelJob(appDepType))
            JSDLUtils.getOrCreateSPMDApplication(value).addNewArgument().setStringValue(stringPrm);
        else
            JSDLUtils.getOrCreatePOSIXApplication(value).addNewArgument().setStringValue(stringPrm);

    }

    public static void setApplicationStdErr(JobDefinitionType value, HpcApplicationDeploymentType appDepType,
            String stderr) {
        FileNameType fName = FileNameType.Factory.newInstance();
        fName.setStringValue(stderr);
        if (isParallelJob(appDepType))
            JSDLUtils.getOrCreateSPMDApplication(value).setError(fName);
        else
            JSDLUtils.getOrCreatePOSIXApplication(value).setError(fName);
    }

    public static void setApplicationStdOut(JobDefinitionType value, HpcApplicationDeploymentType appDepType,
            String stderr) {
        FileNameType fName = FileNameType.Factory.newInstance();
        fName.setStringValue(stderr);
        if (isParallelJob(appDepType))
            JSDLUtils.getOrCreateSPMDApplication(value).setOutput(fName);
        else
            JSDLUtils.getOrCreatePOSIXApplication(value).setOutput(fName);
    }

    public static String getApplicationStdOut(JobDefinitionType value, HpcApplicationDeploymentType appDepType) {
        if (isParallelJob(appDepType))
            return JSDLUtils.getOrCreateSPMDApplication(value).getOutput().getStringValue();
        else
            return JSDLUtils.getOrCreatePOSIXApplication(value).getOutput().getStringValue();
    }

    public static String getApplicationStdErr(JobDefinitionType value, HpcApplicationDeploymentType appDepType) {
        if (isParallelJob(appDepType))
            return JSDLUtils.getOrCreateSPMDApplication(value).getError().getStringValue();
        else
            return JSDLUtils.getOrCreatePOSIXApplication(value).getError().getStringValue();
    }

    public static void createGenericApplication(JobDefinitionType value, HpcApplicationDeploymentType appDepType) {
        if (appDepType.getApplicationName() != null) {
            ApplicationType appType = JSDLUtils.getOrCreateApplication(value);
            String appName = appDepType.getApplicationName().getStringValue();
            appType.setApplicationName(appName);
            JSDLUtils.getOrCreateJobIdentification(value).setJobName(appName);
        }
    }

    public static String getValueFromMap(HpcApplicationDeploymentType appDepType, String name) {
        ExtendedKeyValueType[] extended = appDepType.getKeyValuePairsArray();
        for (ExtendedKeyValueType e : extended) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e.getStringValue();
            }
        }
        return null;
    }

}
