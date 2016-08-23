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
package org.apache.airavata.gfac.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacException extends Exception {
    private static final Logger log = LoggerFactory.getLogger(GFacException.class);

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public GFacException(String s) {
        super(s);
    }

    public GFacException(Exception e) {
        super(e);
        log.error(e.getMessage(),e);
    }

	public GFacException(String s, Throwable throwable) {
        super(s, throwable);
        log.error(s,throwable);
    }
}
