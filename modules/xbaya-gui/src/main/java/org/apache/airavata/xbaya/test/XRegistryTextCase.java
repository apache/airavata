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

package org.apache.airavata.xbaya.test;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.utils.XRegistryClientException;

import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;
import xsul5.MLogger;

public class XRegistryTextCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @throws XregistryException
     * @throws ComponentRegistryException
     */
    public void testClient() throws XRegistryClientException, ComponentRegistryException {
        XRegistryClient client = new XRegistryClient(XBayaConstants.DEFAULT_XREGISTRY_URL.toString());
        ServiceDescData[] abstractServices = client.findServiceDesc("");
        logger.info("abstractServices: " + abstractServices);
        for (ServiceDescData abstractService : abstractServices) {
            logger.info("abstractService: " + abstractService);
        }

        WsdlData[] concreteServices = client.findServiceInstance("");
        logger.info("concreteServices: " + concreteServices);
        for (WsdlData concreteService : concreteServices) {
            logger.info("concreteService: " + concreteService);
        }
    }
}