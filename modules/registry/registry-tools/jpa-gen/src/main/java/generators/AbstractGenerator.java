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

import java.util.List;

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

public class AbstractGenerator {
	private static final String TAB="\t";
	
	protected String removeLastChar(String s) {
		return s.substring(0, s.length()-1);
	}
	
	protected String addLines(String s, String...lines){
		for (String line : lines) {
			s=((s==null||s.equals(""))?"":s+"\n")+line;
		}
		return s;
	}
	
	protected String convertToJavaTitleCaseStringConvention(String s){
		String result="";
		String[] split = s.split("_");
		for (String item : split) {
			result+=(item.toUpperCase().substring(0,1)+(item.length()>1?item.toLowerCase().substring(1):""));
		}
		return result;
	}
	
	protected String convertToJavaVariableNameCaseStringConvention(String s){
		String result=null;
		String[] split = s.split("_");
		for (String item : split) {
			result=(result==null?item.toLowerCase():result+(item.toUpperCase().substring(0,1)+(item.length()>1?item.toLowerCase().substring(1):"")));
		}
		return result;
	}
	
	protected String convertToJavaConstantNameCaseStringConvention(String s){
		String result="";
		for (int i = 0; i < s.length(); i++) {
			String c=String.valueOf(s.charAt(i));
			result+=((c.toUpperCase().equals(c) && !result.equals(""))?"_"+c:c.toUpperCase());
		}
		return result;
	}
	
	protected String convertToTitleCaseString(String s){
		String result="";
		for (int i = 0; i < s.length(); i++) {
			String c=String.valueOf(s.charAt(i));
			result+=((c.toUpperCase().equals(c) && !result.equals(""))?" ":"")+c;
		}
		return result;
	}
	
	protected String tabs(int n){
		String result="";
		for (int i = 0; i < n; i++) {
			result+=TAB;
		}
		return result;
	}
	
	protected String commaSeperatedString(List<String> list, String delimiter){
		String result=null;
		for (String s : list) {
			result=(result==null?s:result+delimiter+s);
		}
		return result;
	}
	
	protected String createFieldVarString(String dataType, String fieldName){
		return "private " + dataType + " " + fieldName + ";";
	}
	
	protected String createSetterString(int indents,String fieldName,
			String dataType, String fieldTitleString) {
	    String setterString=null;
		setterString=addLines(setterString, tabs(indents));
		setterString=addLines(setterString,tabs(indents)+"public void set"+fieldTitleString+"("+dataType+" "+fieldName+") {");
		setterString=addLines(setterString,tabs(indents+1)+"this."+fieldName+"="+fieldName+";");
		setterString=addLines(setterString,tabs(indents)+"}");
		return setterString;
	}

	protected String createGetterString(int indents, String fieldName,
			String dataType, String fieldTitleString) {
	    String getterString=null;
		getterString=addLines(getterString, tabs(indents));
		getterString=addLines(getterString,tabs(indents)+"public "+dataType+" get"+fieldTitleString+"() {");
		getterString=addLines(getterString,tabs(indents+1)+"return "+fieldName+";");
		getterString=addLines(getterString,tabs(indents)+"}");
		return getterString;
	}
	
	protected String createVarNameFromClassName(String s){
		return s.substring(0,1).toLowerCase()+s.substring(1);
	}
}
