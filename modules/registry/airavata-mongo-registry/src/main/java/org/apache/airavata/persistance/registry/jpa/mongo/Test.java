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
package org.apache.airavata.persistance.registry.jpa.mongo;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.persistance.registry.jpa.mongo.dao.ExperimentDao;
import org.apache.airavata.persistance.registry.jpa.mongo.utils.MongoUtil;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test {
    private final static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws RegistryException, IOException {
        AiravataUtils.setExecutionAsServer();
        Registry registry = RegistryFactory.getDefaultRegistry();
//        String experiemtnId = "SLM-Espresso-Stampede_45667ea8-aae3-4a8e-807d-a16312035c35";
//        long time1 = System.currentTimeMillis();
//        Experiment experiement = (Experiment) registry.getExperiment(RegistryModelType.EXPERIMENT, experiemtnId);
//        long time2 = System.currentTimeMillis();
//        System.out.println(time2-time1);
//
//        ExperimentDao experimentDao = new ExperimentDao();
//        experimentDao.createExperiment(experiement);
//        time1 = System.currentTimeMillis();
//        Experiment persistedExperiment = experimentDao.getExperiment(experiement.getExperimentId());
//        time2 = System.currentTimeMillis();
//        System.out.println(time2-time1);
//
//        Assert.assertEquals(experiement, persistedExperiment);

        MongoUtil.dropAiravataRegistry();

        ExperimentDao experimentDao = new ExperimentDao();
        BufferedReader reader = new BufferedReader(new FileReader("/home/supun/Downloads/EXPERIMENT.csv"));
        String temp = reader.readLine();
        int i = 1;
        while(temp != null && !temp.isEmpty()){
            try{
                Experiment experiement = (Experiment) registry.get(RegistryModelType.EXPERIMENT, temp.trim());
                experimentDao.createExperiment(experiement);
                Experiment persistedExperiment = experimentDao.getExperiment(experiement.getExperimentId());
                Assert.assertEquals(experiement, persistedExperiment);
                System.out.println(i+" :"+experiement.getExperimentId());
                i++;
            }catch (Exception e){
                System.out.println(temp);
                e.printStackTrace();
            }
            temp = reader.readLine();
        }
    }
}