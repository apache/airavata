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

package org.apache.airavata.persistance.registry.mongo.repository;

import org.apache.airavata.model.workspace.User;
import org.apache.airavata.persistance.registry.mongo.dao.UserDao;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepository {
    private final static Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private UserDao userDao;

    public UserRepository(){
        this.userDao = new UserDao();
    }

    public String addUser (User user) throws RegistryException {
        try {
            userDao.createUser(user);
            return user.getUserName();
        }catch (Exception e){
            logger.error("Error while saving user to registry", e);
            throw new RegistryException(e);
        }
    }

    public User getUser(String userName) throws RegistryException {
        try {
            return userDao.getUser(userName);
        }catch (Exception e){
            logger.error("Error while retrieving user from registry", e);
            throw new RegistryException(e);
        }
    }

    public boolean isUserExists(String userName) throws RegistryException {
        try {
            return userDao.getUser(userName) != null;
        }catch (Exception e){
            logger.error("Error while retrieving user from registry", e);
            throw new RegistryException(e);
        }
    }
}
