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
import org.apache.thrift.TFieldRequirementType;
import org.junit.Assert;
import org.junit.Test;

public class CustomBeanFactoryTest {
    
    @Test
    public void testRequiredFieldWithDefault() {
        Assert.assertEquals(TFieldRequirementType.REQUIRED, UserConfigurationDataModel.metaDataMap.get(UserConfigurationDataModel._Fields.AIRAVATA_AUTO_SCHEDULE).requirementType);
        UserConfigurationDataModel fromConstructor = new UserConfigurationDataModel();
        Assert.assertFalse(fromConstructor.isSetAiravataAutoSchedule());

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        UserConfigurationDataModel fromFactory = (UserConfigurationDataModel) customBeanFactory
                .createBean(null, null, UserConfigurationDataModel.class.getName());
        Assert.assertTrue(fromFactory.isSetAiravataAutoSchedule());
    }

    @Test
    public void testOptionalFieldWithDefault() {
        Assert.assertEquals(TFieldRequirementType.OPTIONAL, UserConfigurationDataModel.metaDataMap.get(UserConfigurationDataModel._Fields.SHARE_EXPERIMENT_PUBLICLY).requirementType);
        UserConfigurationDataModel fromConstructor = new UserConfigurationDataModel();
        Assert.assertFalse(fromConstructor.isSetShareExperimentPublicly());

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        UserConfigurationDataModel fromFactory = (UserConfigurationDataModel) customBeanFactory
                .createBean(null, null, UserConfigurationDataModel.class.getName());
        Assert.assertTrue(fromFactory.isSetShareExperimentPublicly());
    }
}
