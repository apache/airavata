/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.utils;

import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertNotNull(
                new UserConfigurationDataModel()
                        .getFieldValue(UserConfigurationDataModel._Fields.AIRAVATA_AUTO_SCHEDULE),
                "airavataAutoSchedule has default value");
        Assertions.assertNotNull(
                new UserConfigurationDataModel()
                        .getFieldValue(UserConfigurationDataModel._Fields.OVERRIDE_MANUAL_SCHEDULED_PARAMS),
                "overrideManualScheduledParams has default value");
        Assertions.assertNotNull(
                new UserConfigurationDataModel()
                        .getFieldValue(UserConfigurationDataModel._Fields.SHARE_EXPERIMENT_PUBLICLY),
                "shareExperimentPublicly has default value");
        UserConfigurationDataModel userConfigurationDataModel = ObjectMapperSingleton.getInstance()
                .map(testUserConfigurationDataModel, UserConfigurationDataModel.class);

        Assertions.assertTrue(userConfigurationDataModel.isSetAiravataAutoSchedule());
        Assertions.assertFalse(userConfigurationDataModel.isAiravataAutoSchedule());
        Assertions.assertTrue(userConfigurationDataModel.isSetOverrideManualScheduledParams());
        Assertions.assertFalse(userConfigurationDataModel.isOverrideManualScheduledParams());
        Assertions.assertTrue(userConfigurationDataModel.isSetShareExperimentPublicly());
        Assertions.assertFalse(userConfigurationDataModel.isShareExperimentPublicly());
    }
}
