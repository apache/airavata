/**
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
 */
package org.apache.airavata.testsuite.multitenantedairavata;

import java.util.Map;

public class TestFrameworkProps {
    private int gcount;
    private String gname;
    private String gdomain;
    private String testUserName;
    private String testProjectName;
    private String sshPubKeyLoc;
    private String sshPrivateKeyLoc;
    private String sshPassword;
    private String sshUsername;
    private String tokenFileLoc;
    private String resultFileLoc;
    private boolean injectErrors;
    private Resource[] resources;
    private Application[] applications;
    private Error[] errors;
    private int numberOfIterations;


    public TestFrameworkProps() {
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public int getGcount() {
        return gcount;
    }

    public void setGcount(int gcount) {
        this.gcount = gcount;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }

    public String getGdomain() {
        return gdomain;
    }

    public void setGdomain(String gdomain) {
        this.gdomain = gdomain;
    }

    public String getTestUserName() {
        return testUserName;
    }

    public void setTestUserName(String testUserName) {
        this.testUserName = testUserName;
    }

    public String getTestProjectName() {
        return testProjectName;
    }

    public void setTestProjectName(String testProjectName) {
        this.testProjectName = testProjectName;
    }

    public String getSshPubKeyLoc() {
        return sshPubKeyLoc;
    }

    public void setSshPubKeyLoc(String sshPubKeyLoc) {
        this.sshPubKeyLoc = sshPubKeyLoc;
    }

    public String getSshPrivateKeyLoc() {
        return sshPrivateKeyLoc;
    }

    public void setSshPrivateKeyLoc(String sshPrivateKeyLoc) {
        this.sshPrivateKeyLoc = sshPrivateKeyLoc;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public String getTokenFileLoc() {
        return tokenFileLoc;
    }

    public void setTokenFileLoc(String tokenFileLoc) {
        this.tokenFileLoc = tokenFileLoc;
    }

    public String getResultFileLoc() {
        return resultFileLoc;
    }

    public void setResultFileLoc(String resultFileLoc) {
        this.resultFileLoc = resultFileLoc;
    }


    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public Application[] getApplications() {
        return applications;
    }

    public void setApplications(Application[] applications) {
        this.applications = applications;
    }

    public Error[] getErrors() {
        return errors;
    }

    public void setErrors(Error[] errors) {
        this.errors = errors;
    }

    public boolean isInjectErrors() {
        return injectErrors;
    }

    public void setInjectErrors(boolean injectErrors) {
        this.injectErrors = injectErrors;
    }

    public class Resource {
        private String name;
        private String loginUser;

        public Resource() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLoginUser() {
            return loginUser;
        }

        public void setLoginUser(String loginUser) {
            this.loginUser = loginUser;
        }
    }

    public class Application {
        private String name;
        private Map<String, String> inputs;

        public Application() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getInputs() {
            return inputs;
        }

        public void setInputs(Map<String, String> inputs) {
            this.inputs = inputs;
        }
    }

    public class Error {
        private String name;
        private String application;
        private String resoureName;
        private Map<String, String> errorFeeds;

        public Error() {
        }

        public String getResoureName() {
            return resoureName;
        }

        public void setResoureName(String resoureName) {
            this.resoureName = resoureName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public Map<String, String> getErrorFeeds() {
            return errorFeeds;
        }

        public void setErrorFeeds(Map<String, String> errorFeeds) {
            this.errorFeeds = errorFeeds;
        }
    }
}


