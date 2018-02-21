package org.apache.airavata.helix.core.util;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskUtil {

    public static <T extends AbstractTask> List<OutPort> getOutPortsOfTask(T task) throws IllegalAccessException {
        Field[] fields = task.getClass().getDeclaredFields();
        List<OutPort> outPorts = new ArrayList<>();
        for (Field field : fields) {
            TaskOutPort outPortAnnotation = field.getAnnotation(TaskOutPort.class);
            if (outPortAnnotation != null) {
                field.setAccessible(true);
                OutPort outPort = (OutPort) field.get(task);
                outPorts.add(outPort);
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
                    result.put(parm.name(), classField.get(data).toString());
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

    public static <T extends AbstractTask> void deserializeTaskData(T instance, Map<String, String> params) throws IllegalAccessException, InstantiationException {

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
                    } else if (classField.getType().isAssignableFrom(Integer.class)) {
                        classField.set(instance, Integer.parseInt(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Long.class)) {
                        classField.set(instance, Long.parseLong(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Boolean.class)) {
                        classField.set(instance, Boolean.parseBoolean(params.get(param.name())));
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
}
