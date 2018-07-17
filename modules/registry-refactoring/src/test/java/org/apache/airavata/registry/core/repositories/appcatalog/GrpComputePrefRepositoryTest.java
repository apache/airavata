/*
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

package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpComputePrefRepositoryTest extends TestBase {
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepository.class);
    private final GrpComputePrefRepository grpComputePrefRepository = new GrpComputePrefRepository();
    private final ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
    private final GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
    private final GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();

    public GrpComputePrefRepositoryTest() {
        super(Database.APP_CATALOG);
    }

    @Test
    public void testGwyNoLoginUserNameAndGroupComputePrefNoLoginUserNameFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyNoLoginUserNameAndGroupComputePrefHasLoginUserNamePassesValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName(loginUserName);

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
        } catch (Exception e) {
            Assert.fail("Should have passed validation");
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyNoLoginUserNameAndGroupComputePrefNoLoginUserNameHasAllocationNumberFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        final String allocationProjectNumber = "test-allocation-number";
        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefNoLoginUserNameFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefHasSameLoginUserNameFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName(loginUserName);
        Assert.assertEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefNoLoginUserNameHasAllocationNumberPassesValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        final String allocationProjectNumber = "test-allocation-project-number";
        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
        } catch (Exception e) {
            Assert.fail("Should have passed validation");
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefHasSameLoginUserNameHasAllocationNumberPassesValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        final String allocationProjectNumber = "test-allocation-project-number";
        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName(loginUserName);
        groupComputeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);
        Assert.assertEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
        } catch (Exception e) {
            Assert.fail("Should have passed validation");
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefHasDifferentLoginUserNameHasAllocationNumberPassesValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        final String allocationProjectNumber = "test-allocation-project-number";
        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName("test-login-username2");
        groupComputeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);
        Assert.assertNotEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
        } catch (Exception e) {
            Assert.fail("Should have passed validation");
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testGwyHasLoginUserNameAndGroupComputePrefHasDifferentLoginUserNamePassesValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName("test-login-username2");
        Assert.assertNotEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.validateGroupComputeResourcePreference(groupComputeResourcePreference, gatewayId);
        } catch (Exception e) {
            Assert.fail("Should have passed validation");
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testCreateCallsValidationAndFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName(loginUserName);
        Assert.assertEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.create(groupComputeResourcePreference);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testUpdateCallsValidationAndFailsValidation() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("test-compute-hostname");
        String computeResourceId = computeResourceRepository.addComputeResource(description);
        description.setComputeResourceId(computeResourceId);

        final String gatewayId = "test-gateway-id";
        final String loginUserName = "test-login-username";
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setLoginUserName(loginUserName);
        gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);

        gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);

        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePreference.setComputeResourceId(computeResourceId);
        groupComputeResourcePreference.setLoginUserName("test-login-username2");
        Assert.assertNotEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        grpComputePrefRepository.create(groupComputeResourcePreference);
        groupComputeResourcePreference.setLoginUserName(loginUserName);
        Assert.assertEquals(groupComputeResourcePreference.getLoginUserName(), computeResourcePreference.getLoginUserName());

        try {
            grpComputePrefRepository.update(groupComputeResourcePreference);
            Assert.fail("Should have failed validation");
        } catch (Exception e) {
        }

        gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
        gwyResourceProfileRepository.removeGatewayResourceProfile(gatewayId);
        computeResourceRepository.removeComputeResource(computeResourceId);
    }
}
