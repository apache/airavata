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

package org.apache.airavata.registry.core.utils;

import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.junit.Assert;
import org.junit.Test;

public class ObjectMapperSingletonTest {

    public static class TestUserConfigurationDataModel {
        private boolean airavataAutoSchedule;

        public boolean isAiravataAutoSchedule() {
            return airavataAutoSchedule;
        }

        public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
            this.airavataAutoSchedule = airavataAutoSchedule;
        }
    }

    @Test
    public void testCopyBooleanFieldsWithDefaultValue() {

        TestUserConfigurationDataModel testUserConfigurationDataModel = new TestUserConfigurationDataModel();
        testUserConfigurationDataModel.setAiravataAutoSchedule(false);

        // Make sure these fields have default values
        Assert.assertNotNull("airavataAutoSchedule has default value", new UserConfigurationDataModel().getFieldValue(UserConfigurationDataModel._Fields.AIRAVATA_AUTO_SCHEDULE));
        Assert.assertNotNull("overrideManualScheduledParams has default value", new UserConfigurationDataModel().getFieldValue(UserConfigurationDataModel._Fields.OVERRIDE_MANUAL_SCHEDULED_PARAMS));
        Assert.assertNotNull("shareExperimentPublicly has default value", new UserConfigurationDataModel().getFieldValue(UserConfigurationDataModel._Fields.SHARE_EXPERIMENT_PUBLICLY));
        UserConfigurationDataModel userConfigurationDataModel = ObjectMapperSingleton.getInstance()
                .map(testUserConfigurationDataModel, UserConfigurationDataModel.class);

        Assert.assertTrue(userConfigurationDataModel.isSetAiravataAutoSchedule());
        Assert.assertFalse(userConfigurationDataModel.isAiravataAutoSchedule());
        Assert.assertTrue(
                "even though overrideManualScheduledParams isn't a field on the source object, since it has a default value it should be set",
                userConfigurationDataModel.isSetOverrideManualScheduledParams());
        Assert.assertFalse(userConfigurationDataModel.isOverrideManualScheduledParams());
        Assert.assertTrue(
                "even though shareExperimentPublicly isn't a field on the source object, since it has a default value it should be set",
                userConfigurationDataModel.isSetShareExperimentPublicly());
        Assert.assertFalse(userConfigurationDataModel.isShareExperimentPublicly());
    }
}
