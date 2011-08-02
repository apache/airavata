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

package org.apache.airavata.xregistry.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.TimerTask;

import org.apache.airavata.xregistry.SQLConstants;
import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;

import xsul.MLogger;

public class CWsdlUpdateTask extends TimerTask{
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    private GlobalContext context;
    public CWsdlUpdateTask(GlobalContext context){
        this.context = context;
    }
    
    @Override
    public void run() {
        try {
            Connection connection = null;
            try {
                connection = context.createConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_CWSDL_SQL);
                statement.setLong(1, System.currentTimeMillis());
                int updatedCount = statement.executeUpdate();
                log.info("Ran Cwsdl update "+ updatedCount + " cwsdls deleted");
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(connection != null){
                    context.closeConnection(connection);
                }
            }
        } catch (XregistryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

