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

package org.apache.airavata.core.gfac.factory;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.services.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Factory to create a generic service
 * 
 */
public abstract class AbstractServiceFactory {

    protected final Logger log = LoggerFactory.getLogger(AbstractServiceFactory.class);

    /**
     * Create and initialize a generic service
     * 
     * @return the generic service
     * @throws GfacException
     */
    public final GenericService createService() throws GfacException {
        log.debug("Try to get GenericService");
        GenericService service = getGenericService();
        log.debug("Done get, Try to init GenericService");
        service.init();
        log.debug("Done init GenericService");
        return getGenericService();
    }

    /**
     * Get a service of specific type
     * 
     * @return
     * @throws GfacException
     */
    protected abstract GenericService getGenericService() throws GfacException;
}
