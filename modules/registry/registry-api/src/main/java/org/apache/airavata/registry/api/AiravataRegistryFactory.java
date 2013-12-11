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

package org.apache.airavata.registry.api;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.registry.api.exception.*;
import org.apache.airavata.registry.api.util.RegistrySettings;

import java.net.URI;

public class AiravataRegistryFactory {

	private static final String REGISTRY_ACCESSOR_CLASS = "class.registry.accessor";
	private static AiravataRegistryConnectionDataProvider dataProvider;

	/***
	 * Return a registry accessor object capable of handling all data in the
	 * registry
	 * @param gateway
	 * @param user
	 * @return
	 * @throws RegistryAccessorNotFoundException
	 * @throws RegistryAccessorUndefinedException
	 * @throws RegistryAccessorInstantiateException
	 * @throws AiravataConfigurationException
	 * @throws RegistryAccessorInvalidException
	 */
	public static AiravataRegistry2 getRegistry(Gateway gateway,
			AiravataUser user) throws RegistryException,
            RegistryAccessorUndefinedException,
            RegistryAccessorInstantiateException,
            AiravataConfigurationException, RegistryAccessorInvalidException {
		return getRegistry(null, gateway, user, null);
	}
	
	/***
	 * Return a registry accessor object capable of handling all data in the
	 * registry
	 * @param connectionURI
	 * @param gateway
	 * @param user
	 * @param callback
	 * @return
	 * @throws RegistryAccessorNotFoundException
	 * @throws RegistryAccessorUndefinedException
	 * @throws RegistryAccessorInstantiateException
	 * @throws AiravataConfigurationException
	 * @throws RegistryAccessorInvalidException
	 */
	public static AiravataRegistry2 getRegistry(URI connectionURI, Gateway gateway,
			AiravataUser user, PasswordCallback callback) throws RegistryException,
            RegistryAccessorUndefinedException,
            RegistryAccessorInstantiateException,
            AiravataConfigurationException, RegistryAccessorInvalidException {
		Object registryObj = getRegistryClass(REGISTRY_ACCESSOR_CLASS);
		if (registryObj instanceof AiravataRegistry2) {
			AiravataRegistry2 registry = (AiravataRegistry2) registryObj;
			registry.preInitialize(connectionURI, gateway, user, callback);
			registry.initialize();
			return registry;
		}
		throw new RegistryAccessorInvalidException(registryObj.getClass().getName());
	}

	/***
	 * Given the key in the registry settings file it will
	 * attempt to instantiate a class from the value of the property
	 *
	 * @param registryClassKey
	 * @return
	 * @throws RegistryAccessorNotFoundException
	 * @throws RegistryAccessorUndefinedException
	 * @throws RegistryAccessorInstantiateException
	 * @throws AiravataConfigurationException
	 */
	public static Object getRegistryClass(String registryClassKey)
			throws RegistryAccessorNotFoundException,
			RegistryAccessorUndefinedException,
			RegistryAccessorInstantiateException,
			AiravataConfigurationException {

        try {
            String regAccessorClass = RegistrySettings.getSetting(registryClassKey);
            if (regAccessorClass == null) {
                throw new RegistryAccessorUndefinedException();
            } else {
                try {
                    Class<?> classInstance = AiravataRegistryFactory.class
                            .getClassLoader().loadClass(
                                    regAccessorClass);
                    return classInstance.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new RegistryAccessorNotFoundException(
                            regAccessorClass, e);
                } catch (InstantiationException e) {
                    throw new RegistryAccessorInstantiateException(
                            regAccessorClass, e);
                } catch (IllegalAccessException e) {
                    throw new RegistryAccessorInstantiateException(
                            regAccessorClass, e);
                }
            }
        } catch (RegistrySettingsException e) {
            throw new AiravataConfigurationException(
                    "Error reading the configuration file", e);
        }
    }

	public static void registerRegistryConnectionDataProvider(AiravataRegistryConnectionDataProvider provider){
		dataProvider=provider;
	}

	public static void unregisterRegistryConnectionDataProvider(){
		dataProvider=null;
	}

	public static AiravataRegistryConnectionDataProvider getRegistryConnectionDataProvider(){
		return dataProvider;
	}

}
