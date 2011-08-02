/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry;

import xsul.MLogger;

public class XregistryException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5383629368661247471L;
	protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    public XregistryException() {
        super();
    }

    public XregistryException(String message, Throwable cause) {
        super(message, cause);
        log.caught(cause);
    }

    public XregistryException(String message) {
        super(message);
    }

    public XregistryException(Throwable cause) {
        super(cause);
        log.caught(cause);
    }

}

