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
 */
package org.apache.airavata.helix.core.util;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.TaskParamType;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskUtil {

    private final static Logger logger = LoggerFactory.getLogger(TaskUtil.class);

    public static <T extends AbstractTask> List<OutPort> getOutPortsOfTask(T taskObj) throws IllegalAccessException {

        List<OutPort> outPorts = new ArrayList<>();
        for (Class<?> c = taskObj.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                TaskOutPort outPortAnnotation = field.getAnnotation(TaskOutPort.class);
                if (outPortAnnotation != null) {
                    field.setAccessible(true);
                    OutPort outPort = (OutPort) field.get(taskObj);
                    outPorts.add(outPort);
                }
            }
        }
        return outPorts;
    }

    public static <T extends AbstractTask> Map<String, String> serializeTaskData(T data) throws IllegalAccessException {

        Map<String, String> result = new HashMap<>();
        for (Class<?> c = data.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                TaskParam parm = classField.getAnnotation(TaskParam.class);
                if (parm != null) {
                    classField.setAccessible(true);
                    if (classField.get(data) instanceof TaskParamType) {
                        result.put(parm.name(), TaskParamType.class.cast(classField.get(data)).serialize());
                    } else {
                        result.put(parm.name(), classField.get(data).toString());
                    }
                }

                TaskOutPort outPort = classField.getAnnotation(TaskOutPort.class);
                if (outPort != null) {
                    classField.setAccessible(true);
                    if (classField.get(data) != null) {
                        result.put(outPort.name(), ((OutPort) classField.get(data)).getNextJobId().toString());
                    }
                }
            }
        }
        return result;
    }

    public static <T extends AbstractTask> void deserializeTaskData(T instance, Map<String, String> params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

        List<Field> allFields = new ArrayList<>();
        Class genericClass = instance.getClass();

        while (AbstractTask.class.isAssignableFrom(genericClass)) {
            Field[] declaredFields = genericClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                allFields.add(declaredField);
            }
            genericClass = genericClass.getSuperclass();
        }

        for (Field classField : allFields) {
            TaskParam param = classField.getAnnotation(TaskParam.class);
            if (param != null) {
                if (params.containsKey(param.name())) {
                    classField.setAccessible(true);
                    if (classField.getType().isAssignableFrom(String.class)) {
                        classField.set(instance, params.get(param.name()));
                    } else if (classField.getType().isAssignableFrom(Integer.class) ||
                            classField.getType().isAssignableFrom(Integer.TYPE)) {
                        classField.set(instance, Integer.parseInt(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Long.class) ||
                            classField.getType().isAssignableFrom(Long.TYPE)) {
                        classField.set(instance, Long.parseLong(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Boolean.class) ||
                            classField.getType().isAssignableFrom(Boolean.TYPE)) {
                        classField.set(instance, Boolean.parseBoolean(params.get(param.name())));
                    } else if (TaskParamType.class.isAssignableFrom(classField.getType())) {
                        Class<?> clazz = classField.getType();
                        Constructor<?> ctor = clazz.getConstructor();
                        Object obj = ctor.newInstance();
                        ((TaskParamType)obj).deserialize(params.get(param.name()));
                        classField.set(instance, obj);
                    }
                }
            }
        }

        for (Field classField : allFields) {
            TaskOutPort outPort = classField.getAnnotation(TaskOutPort.class);
            if (outPort != null) {
                classField.setAccessible(true);
                if (params.containsKey(outPort.name())) {
                    classField.set(instance, new OutPort(params.get(outPort.name()), instance));
                } else {
                    classField.set(instance, new OutPort(null, instance));
                }
            }
        }
    }

    public static String replaceSpecialCharacters(String originalTxt, String replaceTxt) {
        return originalTxt.replaceAll("[^a-zA-Z0-9_-]", replaceTxt);
    }
}
