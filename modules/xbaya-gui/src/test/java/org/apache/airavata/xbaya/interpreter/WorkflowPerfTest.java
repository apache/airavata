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
package org.apache.airavata.xbaya.interpreter;

import org.apache.airavata.xbaya.clients.XBayaClient;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkflowPerfTest {
    final static Logger logger = LoggerFactory.getLogger(WorkflowPerfTest.class);

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            logger.info("{} being run...", method.getName());
        }
    };

    @Test
        public void testInvokeWorkflowString(){
         final AtomicInteger completeCount = new AtomicInteger(0);
         int count = 1;
         int repetition = 1;
         try {
               final XBayaClient xBayaClient = new XBayaClient("xbaya.properties");
               xBayaClient.loadWorkflowFromaFile("ranger-echo.xwf");
               final NameValue[] nameValues = xBayaClient.setInputs("xbaya.properties");
               for(int j=0;j<repetition;j++){
               for(int i=0;i<count;i++){
                   new Thread(new Runnable() {
                       public void run() {
                           try {
                               final long time = System.currentTimeMillis();
                               String s = xBayaClient.runWorkflow(UUID.randomUUID().toString(),nameValues);
                               String topic = "test";
                               System.out.println( (System.currentTimeMillis() - time));
                               Assert.assertEquals(topic, s);

                               completeCount.incrementAndGet();
                           } catch (Throwable e) {
                               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                           }
                       }
                   }).start();
                   try {
                       Thread.sleep(1000 / count);
                   } catch (InterruptedException e) {
                       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                   }
               }
               }
} catch (URISyntaxException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
                }}
