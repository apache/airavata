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
package generators;
import java.util.ArrayList;
import java.util.List;

import model.SQLData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SQLGenerator extends AbstractGenerator {
    private static final Logger log = LoggerFactory.getLogger(SQLGenerator.class);
    
	public static enum DataTypes{
		VARCHAR,
		TIMESTAMP,
		INTEGER,
		LONGTEXT,
		SMALLINT,
		CLOB,
	}
	
	public String generateSQLCreateQuery(SQLData sqlData){
		String sql = null;
		sql="CREATE TABLE "+sqlData.getTableName()+"\n";
		sql+="(";
		for (String fieldName : sqlData.getFieldData().keySet()) {
			List<String> fieldData = new ArrayList<String>();
			fieldData.addAll(sqlData.getFieldData().get(fieldName));
			String dataTypeStr = fieldData.get(0);
			fieldData.remove(0);
			DataTypes.valueOf(dataTypeStr);
			sql+="\n\t"+fieldName+" "+dataTypeStr;
			for (String data : fieldData) {
				sql+=" "+data;
			}
			sql+=",";
		}
		
		if (sqlData.getPrimaryKeys().size()>0) {
			sql+="\n\tPRIMARY KEY (";
			for (String primaryKey : sqlData.getPrimaryKeys()) {
				sql+=primaryKey+",";
			}
			sql=removeLastChar(sql);
			sql+="),";
		}
		for (String foriegnKey : sqlData.getForiegnKeys().keySet()) {
			sql+="\n\tFOREIGN KEY ";
			sql+="("+foriegnKey+") REFERENCES "+sqlData.getForiegnKeys().get(foriegnKey).tableAndField+",";
		}
		sql=removeLastChar(sql)+"\n";
		sql+=");";
		return sql;
	}

}
