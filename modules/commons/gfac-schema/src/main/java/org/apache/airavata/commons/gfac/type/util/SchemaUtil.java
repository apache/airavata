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

package org.apache.airavata.commons.gfac.type.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.airavata.commons.gfac.type.Type;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.commons.gfac.type.parameter.BooleanParameter;
import org.apache.airavata.commons.gfac.type.parameter.DoubleParameter;
import org.apache.airavata.commons.gfac.type.parameter.FileParameter;
import org.apache.airavata.commons.gfac.type.parameter.FloatParameter;
import org.apache.airavata.commons.gfac.type.parameter.IntegerParameter;
import org.apache.airavata.commons.gfac.type.parameter.StringParameter;

public class SchemaUtil {
	public static Type parseFromXML(String xml) {
		ByteArrayInputStream bs = new ByteArrayInputStream(xml.getBytes());
		XMLDecoder d = new XMLDecoder(bs);
		Object result = d.readObject();
		d.close();
		return (Type) result;
	}

	public static String toXML(Type type) {
		ByteArrayOutputStream x = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(x);
		e.writeObject(type);
		e.close();
		return x.toString();
	}
	
	public static AbstractParameter mapFromType(DataType type){
	    switch(type){
	    case String:
	        return new StringParameter();
	    case Double:
	        return new DoubleParameter();
	    case Integer:
	        return new IntegerParameter();
	    case Float:
	        return new FloatParameter();
	    case Boolean:
	        return new BooleanParameter();
	    case File:
	        return new FileParameter();
	    }
	    return null;
	}
}
