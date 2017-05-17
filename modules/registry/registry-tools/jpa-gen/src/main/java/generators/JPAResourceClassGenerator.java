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
import model.JPAResourceClassModel;


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

public class JPAResourceClassGenerator extends AbstractGenerator {
	private String exceptionClassName;
	private String jpaUtilsClassName;
	private String resourceTypeClassName;
	private String queryGeneratorClassName;
	
	public JPAResourceClassModel createJPAResourceClassModel(JPAClassModel jpaClassModel){
		JPAResourceClassModel jpaResourceClassModel = new JPAResourceClassModel();
		jpaResourceClassModel.jpaClassModel=jpaClassModel;
		jpaResourceClassModel.className=jpaClassModel.className+"Resource";
		jpaClassModel.classNameConstant=convertToJavaConstantNameCaseStringConvention(jpaClassModel.className);
		for (JPAClassField jpaField : jpaClassModel.fields) {
			jpaField.fieldNameConstant=convertToJavaConstantNameCaseStringConvention(jpaField.fieldName);
		}
		jpaResourceClassModel.jpaClassConstantClassName=jpaClassModel.className+"Constants";
		return jpaResourceClassModel;
	}
	
	public String generateJPAResourceClass(JPAResourceClassModel model){
		String classStr = null;
		String className = model.className;
		classStr=addLines(classStr,"public class "+className+" extends AbstractResource {");
		classStr=addLines(classStr,tabs(1)+"private final static Logger logger = LoggerFactory.getLogger("+className+".class);");

		List<String> columnFields=new ArrayList<String>();
		List<String> fieldGetters=new ArrayList<String>();
		List<String> fieldSetters=new ArrayList<String>();
		for (JPAClassField jpaField : model.jpaClassModel.fields) {
		    String fieldName = jpaField.fieldName;
		    String dataType = jpaField.fieldDataType;
		    String fieldTitleString = jpaField.fieldTitle;
		    
			String fieldString=tabs(1)+createFieldVarString(dataType, fieldName);
		    columnFields.add(fieldString);
		    
		    fieldGetters.add(createGetterString(1, fieldName, dataType, fieldTitleString));
		    fieldSetters.add(createSetterString(1, fieldName, dataType, fieldTitleString));
		    
		    if (jpaField.foriegnKey){
			    fieldName = createVarNameFromClassName(jpaField.foriegnKeyJPAResourceClass);
			    dataType = jpaField.foriegnKeyJPAResourceClass;
			    fieldTitleString = jpaField.foriegnKeyJPAResourceClass;
			    
				fieldString=tabs(1)+createFieldVarString(dataType ,fieldName);
				columnFields.add(fieldString);
				
			    fieldGetters.add(createGetterString(1, fieldName,dataType, fieldTitleString));
			    fieldSetters.add(createSetterString(1, fieldName,dataType, fieldTitleString));
		    }
		    
		}
		classStr=addLines(classStr,columnFields.toArray(new String[]{}));
		
		//remove method
		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public void remove(Object identifier) throws "+getExceptionClassName()+" {");
		if (model.jpaClassModel.generatePKClass){
			classStr=addLines(classStr,tabs(2)+"HashMap<String, String> ids;");
			classStr=addLines(classStr,tabs(2)+"if (identifier instanceof Map) {");
			classStr=addLines(classStr,tabs(3)+"ids = (HashMap<String, String>) identifier;");
			classStr=addLines(classStr,tabs(2)+"} else {");
			classStr=addLines(classStr,tabs(3)+"logger.error(\"Identifier should be a map with the field name and it's value\");");
			classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(\"Identifier should be a map with the field name and it's value\");");
            classStr=addLines(classStr,tabs(2)+"}");	
		}
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");

		classStr=addLines(classStr,tabs(3)+"em.getTransaction().begin();");
		classStr=addLines(classStr,tabs(3)+""+getQueryGeneratorClassName()+" generator = new "+getQueryGeneratorClassName()+"("+model.jpaClassModel.classNameConstant+");");
		if (model.jpaClassModel.generatePKClass){
			for(JPAClassField field:model.jpaClassModel.pkClassModel.pkFields){
				classStr=addLines(classStr,tabs(3)+"generator.setParameter("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+", ids.get("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+"));");
			}
		}else{
			for(JPAClassField field:model.jpaClassModel.fields){
				if (field.primaryKey){
					classStr=addLines(classStr,tabs(3)+"generator.setParameter("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+", identifier);");
				}
			}
		}
		classStr=addLines(classStr,tabs(3)+"Query q = generator.deleteQuery(em);");
		classStr=addLines(classStr,tabs(3)+"q.executeUpdate();");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(2)+"} catch (ApplicationSettingsException e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
        
		classStr=addLines(classStr,tabs(1)+"}");
		
		//get method for resource class
		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public Resource get(Object identifier) throws "+getExceptionClassName()+" {");
		
		if (model.jpaClassModel.generatePKClass){
			classStr=addLines(classStr,tabs(2)+"HashMap<String, String> ids;");
			classStr=addLines(classStr,tabs(2)+"if (identifier instanceof Map) {");
			classStr=addLines(classStr,tabs(3)+"ids = (HashMap<String, String>) identifier;");
			classStr=addLines(classStr,tabs(2)+"} else {");
			classStr=addLines(classStr,tabs(3)+"logger.error(\"Identifier should be a map with the field name and it's value\");");
			classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(\"Identifier should be a map with the field name and it's value\");");
            classStr=addLines(classStr,tabs(2)+"}");	
		}
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");

		classStr=addLines(classStr,tabs(3)+"em.getTransaction().begin();");
		classStr=addLines(classStr,tabs(3)+""+getQueryGeneratorClassName()+" generator = new "+getQueryGeneratorClassName()+"("+model.jpaClassModel.classNameConstant+");");
		if (model.jpaClassModel.generatePKClass){
			for(JPAClassField field:model.jpaClassModel.pkClassModel.pkFields){
				classStr=addLines(classStr,tabs(3)+"generator.setParameter("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+", ids.get("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+"));");
			}
		}else{
			for(JPAClassField field:model.jpaClassModel.fields){
				if (field.primaryKey){
					classStr=addLines(classStr,tabs(3)+"generator.setParameter("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+", identifier);");
				}
			}
		}

		classStr=addLines(classStr,tabs(3)+"Query q = generator.selectQuery(em);");
		String jpaObjVar=createVarNameFromClassName(model.jpaClassModel.className);
		classStr=addLines(classStr,tabs(3)+model.jpaClassModel.className+" "+jpaObjVar+" = ("+model.jpaClassModel.className+") q.getSingleResult();");
		String jpaObjVarResource=createVarNameFromClassName(model.className);
		classStr=addLines(classStr,tabs(3)+model.className+" "+jpaObjVarResource+" = ("+model.className+") "+getJpaUtilsClassName()+".getResource("+getResourceTypeClassName()+"."+model.jpaClassModel.classNameConstant+", "+jpaObjVar+");");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"return "+jpaObjVarResource+";");
		classStr=addLines(classStr,tabs(2)+"} catch (ApplicationSettingsException e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(1)+"}");

		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public List<Resource> get(String fieldName, Object value) throws "+getExceptionClassName()+" {");
		
		String resultListVarName=createVarNameFromClassName(model.className)+"s";
		classStr=addLines(classStr,tabs(2)+"List<Resource> "+resultListVarName+" = new ArrayList<Resource>();");
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");

		classStr=addLines(classStr,tabs(3)+"em.getTransaction().begin();");
		classStr=addLines(classStr,tabs(3)+""+getQueryGeneratorClassName()+" generator = new "+getQueryGeneratorClassName()+"("+model.jpaClassModel.classNameConstant+");");

		classStr=addLines(classStr,tabs(3)+"Query q;");
		List<String> fieldNameValidations=new ArrayList<String>();
		for(JPAClassField field:model.jpaClassModel.fields){
			fieldNameValidations.add("(fieldName.equals("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+"))");
		}
		String fieldNameValidationLogic = commaSeperatedString(fieldNameValidations, " || ");
		classStr=addLines(classStr,tabs(3)+"if ("+fieldNameValidationLogic+") {");
		classStr=addLines(classStr,tabs(4)+"generator.setParameter(fieldName, value);");
		classStr=addLines(classStr,tabs(4)+"q = generator.selectQuery(em);");
		classStr=addLines(classStr,tabs(4)+"List<?> results = q.getResultList();");
		classStr=addLines(classStr,tabs(4)+"for (Object result : results) {");
		classStr=addLines(classStr,tabs(5)+model.jpaClassModel.className+" "+jpaObjVar+" = ("+model.jpaClassModel.className+") result;");
		classStr=addLines(classStr,tabs(5)+model.className+" "+jpaObjVarResource+" = ("+model.className+") "+getJpaUtilsClassName()+".getResource("+getResourceTypeClassName()+"."+model.jpaClassModel.classNameConstant+", "+jpaObjVar+");");
		classStr=addLines(classStr,tabs(5)+resultListVarName+".add("+jpaObjVarResource+");");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(3)+"} else {");
		classStr=addLines(classStr,tabs(4)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(5)+"em.close();");
		classStr=addLines(classStr,tabs(4)+"logger.error(\"Unsupported field name for "+convertToTitleCaseString(model.className)+".\", new IllegalArgumentException());");
		classStr=addLines(classStr,tabs(4)+"throw new IllegalArgumentException(\"Unsupported field name for "+convertToTitleCaseString(model.className)+".\");");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(2)+"} catch (ApplicationSettingsException e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(2)+"return "+resultListVarName+";");
		classStr=addLines(classStr,tabs(1)+"}");

		//id list method
		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public List<String> getIds(String fieldName, Object value) throws "+getExceptionClassName()+" {");
		
		resultListVarName=createVarNameFromClassName(model.className)+"IDs";
		classStr=addLines(classStr,tabs(2)+"List<String> "+resultListVarName+" = new ArrayList<String>();");
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");

		classStr=addLines(classStr,tabs(3)+"em.getTransaction().begin();");
		classStr=addLines(classStr,tabs(3)+""+getQueryGeneratorClassName()+" generator = new "+getQueryGeneratorClassName()+"("+model.jpaClassModel.classNameConstant+");");

		classStr=addLines(classStr,tabs(3)+"Query q;");
		fieldNameValidations=new ArrayList<String>();
		for(JPAClassField field:model.jpaClassModel.fields){
			fieldNameValidations.add("(fieldName.equals("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+"))");
		}
		fieldNameValidationLogic = commaSeperatedString(fieldNameValidations, " || ");
		classStr=addLines(classStr,tabs(3)+"if ("+fieldNameValidationLogic+") {");
		classStr=addLines(classStr,tabs(4)+"generator.setParameter(fieldName, value);");
		classStr=addLines(classStr,tabs(4)+"q = generator.selectQuery(em);");
		classStr=addLines(classStr,tabs(4)+"List<?> results = q.getResultList();");
		classStr=addLines(classStr,tabs(4)+"for (Object result : results) {");
		classStr=addLines(classStr,tabs(5)+model.jpaClassModel.className+" "+jpaObjVar+" = ("+model.jpaClassModel.className+") result;");
		classStr=addLines(classStr,tabs(5)+model.className+" "+jpaObjVarResource+" = ("+model.className+") "+getJpaUtilsClassName()+".getResource("+getResourceTypeClassName()+"."+model.jpaClassModel.classNameConstant+", "+jpaObjVar+");");
		String idFieldToAdd=null;
		if (model.jpaClassModel.generatePKClass){
			for (JPAClassField field : model.jpaClassModel.fields) {
				if (field.foriegnKey){
					idFieldToAdd=jpaObjVarResource+".get"+field.fieldTitle+"()";
					break;
				}
			}
		}else{
			for (JPAClassField field : model.jpaClassModel.fields) {
				if (field.primaryKey){
					idFieldToAdd=jpaObjVarResource+".get"+field.fieldTitle+"()";
				}
			}
		}
		classStr=addLines(classStr,tabs(5)+resultListVarName+".add("+idFieldToAdd+");");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(3)+"} else {");
		classStr=addLines(classStr,tabs(4)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(5)+"em.close();");
		classStr=addLines(classStr,tabs(4)+"logger.error(\"Unsupported field name for "+convertToTitleCaseString(model.className)+".\", new IllegalArgumentException());");
		classStr=addLines(classStr,tabs(4)+"throw new IllegalArgumentException(\"Unsupported field name for "+convertToTitleCaseString(model.className)+".\");");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(2)+"} catch (ApplicationSettingsException e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(2)+"return "+resultListVarName+";");
		
		
		classStr=addLines(classStr,tabs(1)+"}");
		
		//save method
		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public void save() throws "+getExceptionClassName()+" {");
		
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");
		String existingJPAObjVar="existing"+model.jpaClassModel.className;
		
		String primaryKeySearchString=null;
		if (model.jpaClassModel.generatePKClass){
			List<String> fieldStrings=new ArrayList<String>();
			for(JPAClassField field:model.jpaClassModel.pkClassModel.pkFields){
				fieldStrings.add(field.fieldName);
			}
			primaryKeySearchString="new "+model.jpaClassModel.pkClassModel.className+"("+commaSeperatedString(fieldStrings, ", ")+")";
		}else{
			for(JPAClassField field:model.jpaClassModel.fields){
				if (field.primaryKey){
					primaryKeySearchString=field.fieldName;
				}
			}
		}
		classStr=addLines(classStr,tabs(3)+model.jpaClassModel.className+" "+existingJPAObjVar+" = em.find("+model.jpaClassModel.className+".class, "+primaryKeySearchString+");");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(3)+model.jpaClassModel.className+" "+jpaObjVar+";");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().begin();");
		classStr=addLines(classStr,tabs(3)+"if ("+existingJPAObjVar+" == null) {");
		classStr=addLines(classStr,tabs(4)+jpaObjVar+" = new "+model.jpaClassModel.className+"();");
		classStr=addLines(classStr,tabs(3)+"} else {");
		classStr=addLines(classStr,tabs(4)+jpaObjVar+" = "+existingJPAObjVar+";");
		classStr=addLines(classStr,tabs(3)+"}");
		for (JPAClassField field : model.jpaClassModel.fields) {
			classStr=addLines(classStr,tabs(3)+jpaObjVar+".set"+field.fieldTitle+"(get"+field.fieldTitle+"());");
			if (field.foriegnKey){
				String varNameForForiegnKeyObj = createVarNameFromClassName(field.foriegnKeyJPAClass);
				classStr=addLines(classStr,tabs(3)+field.foriegnKeyJPAClass+" "+varNameForForiegnKeyObj+" = em.find("+field.foriegnKeyJPAClass+".class, get"+field.fieldTitle+"());");
				classStr=addLines(classStr,tabs(3)+jpaObjVar+".set"+field.foriegnKeyJPAClass+"("+varNameForForiegnKeyObj+");");
			}
		}
		classStr=addLines(classStr,tabs(3)+"if ("+existingJPAObjVar+" == null) {");
		classStr=addLines(classStr,tabs(4)+"em.persist("+jpaObjVar+");");
		classStr=addLines(classStr,tabs(3)+"} else {");
		classStr=addLines(classStr,tabs(4)+"em.merge("+jpaObjVar+");");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(3)+"em.getTransaction().commit();");
		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(2)+"} catch (Exception e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(1)+"}");
		
		//isexist method

		classStr=addLines(classStr,tabs(1));
		classStr=addLines(classStr,tabs(1)+"@Override");
		classStr=addLines(classStr,tabs(1)+"public boolean isExists(Object identifier) throws "+getExceptionClassName()+" {");
		
		if (model.jpaClassModel.generatePKClass){
			classStr=addLines(classStr,tabs(2)+"HashMap<String, String> ids;");
			classStr=addLines(classStr,tabs(2)+"if (identifier instanceof Map) {");
			classStr=addLines(classStr,tabs(3)+"ids = (HashMap<String, String>) identifier;");
			classStr=addLines(classStr,tabs(2)+"} else {");
			classStr=addLines(classStr,tabs(3)+"logger.error(\"Identifier should be a map with the field name and it's value\");");
			classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(\"Identifier should be a map with the field name and it's value\");");
            classStr=addLines(classStr,tabs(2)+"}");	
		}
        
		primaryKeySearchString=null;
		if (model.jpaClassModel.generatePKClass){
			List<String> fieldStrings=new ArrayList<String>();
			for(JPAClassField field:model.jpaClassModel.pkClassModel.pkFields){
				fieldStrings.add("ids.get("+model.jpaClassConstantClassName+"."+field.fieldNameConstant+")");
			}
			primaryKeySearchString="new "+model.jpaClassModel.pkClassModel.className+"("+commaSeperatedString(fieldStrings, ", ")+")";
		}else{
			for(JPAClassField field:model.jpaClassModel.fields){
				if (field.primaryKey){
					primaryKeySearchString="identifier";
				}
			}
		}
		classStr=addLines(classStr,tabs(2)+"EntityManager em = null;");
		classStr=addLines(classStr,tabs(2)+"try {");
		classStr=addLines(classStr,tabs(3)+"em = "+getJpaUtilsClassName()+".getEntityManager();");
		classStr=addLines(classStr,tabs(3)+model.jpaClassModel.className+" "+jpaObjVar+" = em.find("+model.jpaClassModel.className+".class, "+primaryKeySearchString+");");

		classStr=addLines(classStr,tabs(3)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"return "+jpaObjVar+" != null;");
		classStr=addLines(classStr,tabs(2)+"} catch (ApplicationSettingsException e) {");
		classStr=addLines(classStr,tabs(3)+"logger.error(e.getMessage(), e);");
		classStr=addLines(classStr,tabs(3)+"throw new "+getExceptionClassName()+"(e);");
		classStr=addLines(classStr,tabs(2)+"} finally {");
		classStr=addLines(classStr,tabs(3)+"if (em != null && em.isOpen()) {");
		classStr=addLines(classStr,tabs(4)+"if (em.getTransaction().isActive()) {");
		classStr=addLines(classStr,tabs(5)+"em.getTransaction().rollback();");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(4)+"em.close();");
		classStr=addLines(classStr,tabs(3)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(1)+"}");

		
		classStr=addLines(classStr,fieldGetters.toArray(new String[]{}));
		classStr=addLines(classStr,fieldSetters.toArray(new String[]{}));
		
		classStr=addLines(classStr,"}");
		return classStr;
	}
	
	public String generateAbstractResourceClassUpdates(JPAResourceClassModel model){
		String classStr = null;
		classStr=addLines(classStr,"public abstract class AbstractResource implements Resource {");
		
		classStr=addLines(classStr,tabs(1)+"public static final String "+model.jpaClassModel.classNameConstant+" = \""+model.jpaClassModel.className+"\";");
		
		classStr=addLines(classStr,tabs(1)+"// "+convertToTitleCaseString(model.jpaClassModel.className)+" Table");
		classStr=addLines(classStr,tabs(1)+"public final class "+model.jpaClassConstantClassName+" {");
		for (JPAClassField jpaField : model.jpaClassModel.fields) {
			classStr=addLines(classStr,tabs(2)+"public static final String "+jpaField.fieldNameConstant+" = \""+jpaField.fieldName+"\";");
		}
		classStr=addLines(classStr,tabs(1)+"}");
		classStr=addLines(classStr,"}");
		return classStr;
	}
	
	public String generateAppCatalogJPAUtilUpdates(JPAResourceClassModel model){
		String classStr = null;
		String conversionMethodName="create"+model.jpaClassModel.className;
		classStr=addLines(classStr,"public class "+getJpaUtilsClassName()+" {");
		classStr=addLines(classStr,tabs(1)+"public static Resource getResource("+getResourceTypeClassName()+" type, Object o) {");
		classStr=addLines(classStr,tabs(2)+"switch (type){");
		classStr=addLines(classStr,tabs(3)+"case "+model.jpaClassModel.classNameConstant+":");
		classStr=addLines(classStr,tabs(4)+"if (o instanceof "+model.jpaClassModel.className+"){");
		classStr=addLines(classStr,tabs(5)+"return "+conversionMethodName+"(("+model.jpaClassModel.className+") o);");
		classStr=addLines(classStr,tabs(4)+"}else{");
		classStr=addLines(classStr,tabs(5)+"logger.error(\"Object should be a "+convertToTitleCaseString(model.jpaClassModel.className)+".\", new IllegalArgumentException());");
		classStr=addLines(classStr,tabs(5)+"throw new IllegalArgumentException(\"Object should be a "+convertToTitleCaseString(model.jpaClassModel.className)+".\");");
		classStr=addLines(classStr,tabs(4)+"}");
		classStr=addLines(classStr,tabs(2)+"}");
		classStr=addLines(classStr,tabs(1)+"}");
		classStr=addLines(classStr,tabs(1));
		
		String resourceVariableName = createVarNameFromClassName(model.className);
		classStr=addLines(classStr,tabs(1)+"private static Resource "+conversionMethodName+"("+model.jpaClassModel.className+" o) {");
		classStr=addLines(classStr,tabs(2)+model.className+" "+resourceVariableName+" = new "+model.className+"();");
		for(JPAClassField field:model.jpaClassModel.fields){
			classStr=addLines(classStr,tabs(2)+resourceVariableName+".set"+field.fieldTitle+"(o.get"+field.fieldTitle+"());");
			if (field.foriegnKey){
				classStr=addLines(classStr,tabs(2)+resourceVariableName+".set"+field.foriegnKeyJPAResourceClass+"(("+field.foriegnKeyJPAResourceClass+")create"+field.foriegnKeyJPAClass+"(o.get"+field.foriegnKeyJPAClass+"()));");
			}
		}
		classStr=addLines(classStr,tabs(2)+"return "+resourceVariableName+";");
		classStr=addLines(classStr,tabs(1)+"}");
		
		classStr=addLines(classStr,"}");
		return classStr;
	}
	
	public String generateAppCatalogResourceTypeUpdates(JPAResourceClassModel model){
		String classStr = null;
		classStr=addLines(classStr,"public enum "+getResourceTypeClassName()+" {");
		classStr=addLines(classStr,tabs(1)+model.jpaClassModel.classNameConstant);
		classStr=addLines(classStr,"}");
		return classStr;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public void setExceptionClassName(String exceptionClassName) {
		this.exceptionClassName = exceptionClassName;
	}

	public String getJpaUtilsClassName() {
		return jpaUtilsClassName;
	}

	public void setJpaUtilsClassName(String jpaUtilsClassName) {
		this.jpaUtilsClassName = jpaUtilsClassName;
	}

	public String getResourceTypeClassName() {
		return resourceTypeClassName;
	}

	public void setResourceTypeClassName(String resourceTypeClassName) {
		this.resourceTypeClassName = resourceTypeClassName;
	}

	public String getQueryGeneratorClassName() {
		return queryGeneratorClassName;
	}

	public void setQueryGeneratorClassName(String queryGeneratorClassName) {
		this.queryGeneratorClassName = queryGeneratorClassName;
	}
	

}
