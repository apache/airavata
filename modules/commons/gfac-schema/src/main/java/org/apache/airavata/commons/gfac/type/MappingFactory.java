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

package org.apache.airavata.commons.gfac.type;

import org.apache.airavata.schemas.gfac.BooleanParameter;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.DoubleParameter;
import org.apache.airavata.schemas.gfac.FileParameter;
import org.apache.airavata.schemas.gfac.FloatParameter;
import org.apache.airavata.schemas.gfac.IntegerParameter;
import org.apache.airavata.schemas.gfac.StringParameter;

/*
 * TODO use XML meta data instead of static coding
 * 
 */
public class MappingFactory {
	public static String toString(ActualParameter param){
		if(param.hasType(DataType.STRING)){
			return ((StringParameter)param.getType()).getValue();
		}else if (param.hasType(DataType.INTEGER)){
			return String.valueOf(((IntegerParameter)param.getType()).getValue());
		}else if (param.hasType(DataType.DOUBLE)){
			return String.valueOf(((DoubleParameter)param.getType()).getValue());
		}else if (param.hasType(DataType.BOOLEAN)){
			return String.valueOf(((BooleanParameter)param.getType()).getValue());
		}else if (param.hasType(DataType.FILE)){
			return ((FileParameter)param.getType()).getValue();
		}else if (param.hasType(DataType.FLOAT)){
			return String.valueOf(((FloatParameter)param.getType()).getValue());
		}
		return null;
	}
	
	public static void fromString(ActualParameter param, String val){
		if(param.hasType(DataType.STRING)){
			((StringParameter)param.getType()).setValue(val);
		}else if (param.hasType(DataType.INTEGER)){
			((IntegerParameter)param.getType()).setValue(Integer.parseInt(val));
		}else if (param.hasType(DataType.DOUBLE)){
			((DoubleParameter)param.getType()).setValue(Double.parseDouble(val));
		}else if (param.hasType(DataType.BOOLEAN)){
			((BooleanParameter)param.getType()).setValue(Boolean.parseBoolean(val));
		}else if (param.hasType(DataType.FILE)){
			((FileParameter)param.getType()).setValue(val);
		}else if (param.hasType(DataType.FLOAT)){
			((FloatParameter)param.getType()).setValue(Float.parseFloat(val));
		}
	}
}
