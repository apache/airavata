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
package org.apache.airavata.sharing.registry.utils;

import org.apache.airavata.common.utils.DBEventService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class Constants {
    /**
     * List of publishers in which sharing service is interested.
     * Add publishers as required
     */
    public static final List<String> PUBLISHERS = new ArrayList<String>(){{
        add(DBEventService.USER_PROFILE.toString());
        add(DBEventService.TENANT.toString());
        add(DBEventService.REGISTRY.toString());
        add(DBEventService.IAM_ADMIN.toString());
    }};
}
