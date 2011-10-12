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

package org.apache.airavata.commons.gfac.type.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterFactory {

    private static final Logger log = LoggerFactory.getLogger(ParameterFactory.class);
    private static final String PROPERTIES_NAME = "datatype.properties";
    private static ParameterFactory instance;
    private Map<String, Class<? extends AbstractParameter>> map;
    private Map<String, DataType> typeMap;
    private List<DataType> types;

    private ParameterFactory() {
        /*
         * Load properties of type
         */
        try {
            types = new ArrayList<DataType>();
            map = new HashMap<String, Class<? extends AbstractParameter>>();
            typeMap = new HashMap<String, DataType>(); 
            /*
             * Load properties only it is not loaded
             */
            Configuration config = new PropertiesConfiguration(PROPERTIES_NAME);
            Iterator iterator = config.getKeys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                /*
                 * throw exception for wrong config property file.
                 */
                if (map.containsKey(key)) {
                    log.error("Property file contains duplicate data types");
                    continue;
                }

                try {
                    /*
                     * Initialize association class
                     */
                    Class<? extends AbstractParameter> cl = Class.forName(config.getString(key)).asSubclass(
                            AbstractParameter.class);

                    map.put(key, cl);
                    DataType type = new DataType(key);
                    typeMap.put(key, type);
                    types.add(type);

                } catch (Exception e) {
                    log.error("Cannont find associated class: " + config.getString(key) + " with type: " + key);
                }

            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("unable to load configurations:::" + PROPERTIES_NAME, e);
        }

    }

    public static synchronized ParameterFactory getInstance() {
        if (instance == null) {
            instance = new ParameterFactory();
        }
        return instance;
    }

    public List<DataType> listDataTypes() {
        return types;
    }

    public AbstractParameter createActualParameter(DataType dataType) throws Exception {
        return createActualParameter(dataType.getType());
    }
    
    public AbstractParameter createActualParameter(String type) throws Exception{
        if (!map.containsKey(type))
            throw new RuntimeException("Type is not supprted: " + type);
        Class<? extends AbstractParameter> cl = map.get(type);
        AbstractParameter result = cl.newInstance();
        result.setType(getType(type));
        return result;
    }
    
    public DataType getType(String type){
        if (!typeMap.containsKey(type))
            throw new RuntimeException("Type is not supprted: " + type);
        return typeMap.get(type);
    }

    public boolean hasType(DataType dataType, String type){
        return dataType.getType().equalsIgnoreCase(type);
    }
}
