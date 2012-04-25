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

import org.apache.airavata.schemas.gfac.BooleanArrayType;
import org.apache.airavata.schemas.gfac.BooleanParameterType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.DoubleArrayType;
import org.apache.airavata.schemas.gfac.DoubleParameterType;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.FileParameterType;
import org.apache.airavata.schemas.gfac.FloatArrayType;
import org.apache.airavata.schemas.gfac.FloatParameterType;
import org.apache.airavata.schemas.gfac.IntegerArrayType;
import org.apache.airavata.schemas.gfac.IntegerParameterType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * TODO use XML meta data instead of static coding
 * 
 */
public class MappingFactory {

    /**
     * This method is used to map between ENUM datatype to actual parameter type for example: Enum type String will map
     * to StringParameterType in XMLSchema
     * 
     * @param type
     * @return
     */
    public static String getActualParameterType(DataType.Enum type) {
        if (type.equals(DataType.STRING)) {
            return StringParameterType.class.getSimpleName();
        } else if (type.equals(DataType.INTEGER)) {
            return IntegerParameterType.class.getSimpleName();
        } else if (type.equals(DataType.DOUBLE)) {
            return DoubleParameterType.class.getSimpleName();
        } else if (type.equals(DataType.BOOLEAN)) {
            return BooleanParameterType.class.getSimpleName();
        } else if (type.equals(DataType.FILE)) {
            return FileParameterType.class.getSimpleName();
        } else if (type.equals(DataType.FLOAT)) {
            return FloatParameterType.class.getSimpleName();
        } else if (type.equals(DataType.URI)) {
            return URIParameterType.class.getSimpleName();
        } else if (type.equals(DataType.STRING_ARRAY)) {
            return StringArrayType.class.getSimpleName();
        } else if (type.equals(DataType.INTEGER_ARRAY)) {
            return IntegerArrayType.class.getSimpleName();
        } else if (type.equals(DataType.DOUBLE_ARRAY)) {
            return DoubleArrayType.class.getSimpleName();
        } else if (type.equals(DataType.BOOLEAN_ARRAY)) {
            return BooleanArrayType.class.getSimpleName();
        } else if (type.equals(DataType.FILE_ARRAY)) {
            return FileArrayType.class.getSimpleName();
        } else if (type.equals(DataType.FLOAT_ARRAY)) {
            return FloatArrayType.class.getSimpleName();
        } else if (type.equals(DataType.URI_ARRAY)) {
            return URIArrayType.class.getSimpleName();
        }
        return StringParameterType.class.getSimpleName();
    }

    /**
     * This method is used to map from Actual parameter type to String. It is used for mapping between ParamaterType in
     * XML to command-line application arguments
     * 
     * @param param
     * @return
     */
    public static String toString(ActualParameter param) {
        if (param.hasType(DataType.STRING)) {
            return ((StringParameterType) param.getType()).getValue();
        } else if (param.hasType(DataType.INTEGER)) {
            return String.valueOf(((IntegerParameterType) param.getType()).getValue());
        } else if (param.hasType(DataType.DOUBLE)) {
            return String.valueOf(((DoubleParameterType) param.getType()).getValue());
        } else if (param.hasType(DataType.BOOLEAN)) {
            return String.valueOf(((BooleanParameterType) param.getType()).getValue());
        } else if (param.hasType(DataType.FILE)) {
            return ((FileParameterType) param.getType()).getValue();
        } else if (param.hasType(DataType.FLOAT)) {
            return String.valueOf(((FloatParameterType) param.getType()).getValue());
        } else if (param.hasType(DataType.URI)) {
            return String.valueOf(((URIParameterType) param.getType()).getValue());
        } else if (param.hasType(DataType.STRING_ARRAY)) {
           return join(Arrays.asList(((StringArrayType) param.getType()).getValueArray()),",");
        } else if (param.hasType(DataType.INTEGER_ARRAY)) {
            //todo return proper string array from int,double,boolean arrays
            return String.valueOf(((IntegerArrayType) param.getType()).getValueArray());
        } else if (param.hasType(DataType.DOUBLE_ARRAY)) {
            return String.valueOf(((DoubleArrayType) param.getType()).getValueArray());
        } else if (param.hasType(DataType.BOOLEAN_ARRAY)) {
            return String.valueOf(((BooleanArrayType) param.getType()).getValueArray());
        } else if (param.hasType(DataType.FILE_ARRAY)) {
            return join(Arrays.asList(((FileArrayType) param.getType()).getValueArray()),",");
        } else if (param.hasType(DataType.FLOAT_ARRAY)) {
            return String.valueOf(((FloatArrayType) param.getType()).getValueArray());
        } else if (param.hasType(DataType.URI_ARRAY)) {
           return join(Arrays.asList(((URIArrayType) param.getType()).getValueArray()),",");
        }
        return null;
    }

    /**
     * This method is used to map output from command-line application to actual parameter in XML Schema.
     * 
     * @param param
     * @param val
     */
    public static void fromString(ActualParameter param, String val) {
        if (param.hasType(DataType.STRING)) {
            ((StringParameterType) param.getType()).setValue(val);
        } else if (param.hasType(DataType.INTEGER)) {
            ((IntegerParameterType) param.getType()).setValue(Integer.parseInt(val));
        } else if (param.hasType(DataType.DOUBLE)) {
            ((DoubleParameterType) param.getType()).setValue(Double.parseDouble(val));
        } else if (param.hasType(DataType.BOOLEAN)) {
            ((BooleanParameterType) param.getType()).setValue(Boolean.parseBoolean(val));
        } else if (param.hasType(DataType.FILE)) {
            ((FileParameterType) param.getType()).setValue(val);
        } else if (param.hasType(DataType.FLOAT)) {
            ((FloatParameterType) param.getType()).setValue(Float.parseFloat(val));
        } else if (param.hasType(DataType.URI)) {
            ((URIParameterType) param.getType()).setValue((val));
        }
    }

    public static String join(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for (String s : list) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        return sb.toString();
    }
}
