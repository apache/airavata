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

package org.apache.airavata.common.exception;

public class ApplicationSettingsStoreException extends ApplicationSettingsException {

	private static final long serialVersionUID = -5102090895499711299L;
	public ApplicationSettingsStoreException(String filePath) {
		super("Error while attempting to store settings in "+filePath);
	}

	public ApplicationSettingsStoreException(String filePath, Throwable e) {
		super(filePath,e);
	}
}
