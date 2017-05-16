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

import model.JPAClassField;
import model.JPAClassModel;
import model.JPAPKClassModel;
import model.SQLData;


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

public class JPAClassGenerator extends AbstractGenerator{
//    private static final Logger log = LoggerFactory.getLogger(JPAClassGenerator.class);
	private String jpaClassPackageName;
	
    public JPAClassModel createJPAClassModel(SQLData sqlData){
		JPAClassModel model = new JPAClassModel();
		model.generatePKClass=sqlData.getPrimaryKeys().size()>1;
		model.tableName=sqlData.getTableName();
		model.className = convertToJavaTitleCaseStringConvention(sqlData.getTableName());
		if (model.generatePKClass) {
			model.pkClassModel.className=model.className+"_PK";
		}
		for (String field : sqlData.getFieldData().keySet()) {
		    String dataType = null;
		    SQLGenerator.DataTypes sqlDataType = SQLGenerator.DataTypes.valueOf(sqlData.getFieldData().get(field).get(0));
		    switch (sqlDataType){
		    case LONGTEXT:case VARCHAR:
		    	dataType="String"; break;
		    case INTEGER: 
		    	dataType="int"; break;
		    case SMALLINT:
		    	dataType="boolean"; break;
		    case TIMESTAMP: 
		    	dataType="Timestamp"; break;
		    case CLOB:
		    	dataType="String"; break;
		    }
		    String fieldTitleString = convertToJavaTitleCaseStringConvention(field);
		    String fieldName = convertToJavaVariableNameCaseStringConvention(field);

		    
		    boolean foriegnKey = sqlData.getForiegnKeys().containsKey(field);
			JPAClassField jpaField = new JPAClassField(field,fieldName,dataType,fieldTitleString,sqlData.getPrimaryKeys().contains(field),
		    		foriegnKey,(foriegnKey?sqlData.getForiegnKeys().get(field).jpaClassName:null),
		    		(foriegnKey?sqlData.getForiegnKeys().get(field).jpaResourceClassName:null));
			model.fields.add(jpaField);
		    if (model.generatePKClass){
		    	if (sqlData.getPrimaryKeys().contains(field)){
		    		model.pkClassModel.pkFields.add(jpaField);
		    	}
		    }
		    
		}
		return model;
	}
    
	public String generateJPAClass(JPAClassModel model){
		String classStr = null;
		String pkClassName = null;
		classStr=addLines(classStr,"@DataCache");
		classStr=addLines(classStr,"@Entity");
		classStr=addLines(classStr,"@Table(name = \""+model.tableName+"\")");
		String className = model.className;
		if (model.generatePKClass) {
			pkClassName=model.pkClassModel.className;
			classStr = addLines(classStr,"@IdClass("+pkClassName+".class)");
		}
		classStr=addLines(classStr,"public class "+className+" implements Serializable {");
		
		List<String> columnFields=new ArrayList<String>();
		List<String> fieldGetters=new ArrayList<String>();
		List<String> fieldSetters=new ArrayList<String>();
		for (JPAClassField jpaField : model.fields) {
			String field=jpaField.tableColumnName;
		    String fieldString=null;
		    
		    String fieldName = jpaField.fieldName;
		    String dataType = jpaField.fieldDataType;
		    String fieldTitleString = jpaField.fieldTitle;

		    
		    fieldString=addLines(fieldString, tabs(1));
		    if (jpaField.primaryKey){
		    	fieldString=addLines(fieldString,tabs(1)+"@Id");	
		    }
		    fieldString=addLines(fieldString,tabs(1)+"@Column(name = \""+field+"\")");
			fieldString=addLines(fieldString,tabs(1)+createFieldVarString(dataType ,fieldName));
		    columnFields.add(fieldString);
		    
		    
		    fieldGetters.add(createGetterString(1, fieldName,dataType, fieldTitleString));

		    fieldSetters.add(createSetterString(1, fieldName,dataType, fieldTitleString));
		    
		    if (jpaField.foriegnKey){
			    fieldString=null;
			    
			    fieldName = createVarNameFromClassName(jpaField.foriegnKeyJPAClass);
			    dataType = jpaField.foriegnKeyJPAClass;
			    fieldTitleString = jpaField.foriegnKeyJPAClass;
			    
			    
			    fieldString=addLines(fieldString, tabs(1));
			    fieldString=addLines(fieldString,tabs(1)+"@ManyToOne(cascade= CascadeType.MERGE)");
			    fieldString=addLines(fieldString,tabs(1)+"@JoinColumn(name = \""+jpaField.tableColumnName+"\")");
				fieldString=addLines(fieldString,tabs(1)+createFieldVarString(dataType ,fieldName));
				columnFields.add(fieldString);
				
			    fieldGetters.add(createGetterString(1, fieldName,dataType, fieldTitleString));

			    fieldSetters.add(createSetterString(1, fieldName,dataType, fieldTitleString));
		    }
		}
		classStr=addLines(classStr,columnFields.toArray(new String[]{}));
		classStr=addLines(classStr,fieldGetters.toArray(new String[]{}));
		classStr=addLines(classStr,fieldSetters.toArray(new String[]{}));
		
		classStr=addLines(classStr,"}");
		return classStr;
	}

	public String generateJPAPKClass(JPAPKClassModel model){
		if (model.pkFields.size()==0){
			return "";
		}
		String classStr=null;
		classStr=addLines(classStr,"public class "+model.className+" implements Serializable {");
		
		List<String> columnFields=new ArrayList<String>();
		List<String> fieldGetters=new ArrayList<String>();
		List<String> fieldSetters=new ArrayList<String>();
		List<String> parameterList=new ArrayList<String>();
		String constructorMethod=null;
		for (JPAClassField jpaField : model.pkFields) {
		    
		    String dataType = jpaField.fieldDataType;
		    String fieldTitleString = jpaField.fieldTitle;
		    String fieldName = jpaField.fieldName;
		    
		    String fieldString=tabs(1)+createFieldVarString(dataType ,fieldName);
		    columnFields.add(fieldString);
		    
		    
		    fieldGetters.add(createGetterString(1, fieldName, dataType, fieldTitleString));

		    fieldSetters.add(createSetterString(1, fieldName,	dataType, fieldTitleString));

		    parameterList.add(dataType+" "+fieldName);
		    constructorMethod=addLines(constructorMethod, tabs(2)+"this."+fieldName+" = "+fieldName+";");
		}
		classStr=addLines(classStr,columnFields.toArray(new String[]{}));
		String constructorParametersString=commaSeperatedString(parameterList,", ");
		constructorMethod=addLines(tabs(1), tabs(1)+"public "+model.className+"("+constructorParametersString+"){",constructorMethod);
		constructorMethod=addLines(constructorMethod, tabs(1)+"}");
		String emptyConstructorMethod=null;
		emptyConstructorMethod=addLines(tabs(1),tabs(1)+"public "+model.className+"(){",tabs(1)+"}");
		
		classStr=addLines(classStr,emptyConstructorMethod);
		classStr=addLines(classStr,constructorMethod);
		



		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public boolean equals(Object o) {");
		classStr=addLines(classStr,tabs(2)+"return false;");
		classStr=addLines(classStr,tabs(1)+"}");

		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public int hashCode() {");
		classStr=addLines(classStr,tabs(2)+"return 1;");
		classStr=addLines(classStr,tabs(1)+"}");
	    
		classStr=addLines(classStr,fieldGetters.toArray(new String[]{}));
		classStr=addLines(classStr,fieldSetters.toArray(new String[]{}));
		
		classStr=addLines(classStr,"}");
		return classStr;
	}

	public String generatePersistenceXmlEntry(JPAClassModel model){
		String xmlEntry=null;
		xmlEntry=addLines(xmlEntry,"<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\">");
		xmlEntry=addLines(xmlEntry,tabs(1)+"<persistence-unit name=\"appcatalog_data\">");
		xmlEntry=addLines(xmlEntry,tabs(2)+"<provider>org.apache.openjpa.persistence.PersistenceProviderImpl</provider>");
		xmlEntry=addLines(xmlEntry,tabs(2)+"<class>"+getJpaClassPackageName()+"."+model.className+"</class>");
		xmlEntry=addLines(xmlEntry,tabs(2)+"<exclude-unlisted-classes>true</exclude-unlisted-classes>");
		xmlEntry=addLines(xmlEntry,tabs(1)+"</persistence-unit>");
		xmlEntry=addLines(xmlEntry,"</persistence>");
		return xmlEntry;
	}
	
	public String getJpaClassPackageName() {
		return jpaClassPackageName;
	}

	public void setJpaClassPackageName(String jpaClassPackageName) {
		this.jpaClassPackageName = jpaClassPackageName;
	}
	
}
