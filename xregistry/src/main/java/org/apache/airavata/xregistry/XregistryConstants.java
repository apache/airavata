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

public interface XregistryConstants {
    public static final String LOGGER_NAME = "xregistry.logger";
    public static final String X_REGISTRY_PROPERTY_FILE = "xregistry.properties";
    public static final String XREGISTRY_SQL_FILE = "tables.sql";
    public static final String ANONYMOUS_USER = "/C=US/O=Extreme Lab Indiana University/CN=Anonymous User";
    public static final String PUBLIC_GROUP = "public";
    public static final String DEFAULTPARENT = "default";
    public static final String DEFAULTOGCERESOURCETYPE = "unspecified";
    
    public static enum Action {All,AddNew,Read,Write,ResourceAdmin,SysAdmin};
    
    public static enum DocType{ServiceDesc,AppDesc,HostDesc,CWsdl};
    public static enum SqlParmType{String,Int,Long};
    
}

