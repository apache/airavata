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

package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;

public class AbstractJackRabbitUMComponent {

    protected List<Group> getJRGroupList(Iterator<org.apache.airavata.registry.api.user.Group> airavataUMGroupList) {
        List<Group> groupList = new ArrayList<Group>();
        while (airavataUMGroupList.hasNext()) {
            groupList.add(new JackRabbitGroup(airavataUMGroupList.next()));
        }
        return groupList;
    }

    protected List<org.apache.airavata.registry.api.user.Group> getGroupList(Iterator<Group> airavataUMGroupList) {
        List<org.apache.airavata.registry.api.user.Group> groupList = new ArrayList<org.apache.airavata.registry.api.user.Group>();
        while (airavataUMGroupList.hasNext()) {
            groupList.add(new JackRabbitGroupWrap(airavataUMGroupList.next()));
        }
        return groupList;
    }

    protected List<Authorizable> getJRAuthorizableList(
            Iterator<org.apache.airavata.registry.api.user.Authorizable> jackRabbitAuthorizableList) {
        List<Authorizable> authorizableList = new ArrayList<Authorizable>();
        while (jackRabbitAuthorizableList.hasNext()) {
            authorizableList.add(new JackRabbitAuthorizable(jackRabbitAuthorizableList.next()));
        }
        return authorizableList;
    }

    protected List<org.apache.airavata.registry.api.user.Authorizable> getAuthorizableList(
            Iterator<Authorizable> jackRabbitAuthorizableList) {
        List<org.apache.airavata.registry.api.user.Authorizable> authorizableList = new ArrayList<org.apache.airavata.registry.api.user.Authorizable>();
        while (jackRabbitAuthorizableList.hasNext()) {
            authorizableList.add(new JackRabbitAuthorizableWrap(jackRabbitAuthorizableList.next()));
        }
        return authorizableList;
    }
}
