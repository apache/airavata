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
package org.apache.airavata.cloud.util;

import org.openstack4j.model.compute.FloatingIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackIntfUtil {

	// Initializing Logger
	private static Logger logger = LoggerFactory.getLogger(OpenstackIntfUtil.class);

	public static Boolean isFloatingIPUsed(FloatingIP floatIp) {

		try {

			Boolean isUsed = floatIp.getInstanceId() != null;

			return isUsed;

		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to check if the Floating IP " + floatIp.getFloatingIpAddress() + "is used.");
			return null;
		}
	}

}
