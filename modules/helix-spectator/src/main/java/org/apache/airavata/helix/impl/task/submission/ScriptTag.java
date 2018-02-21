package org.apache.airavata.helix.impl.task.submission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScriptTag {
    public String name();
    public boolean mandatory() default false;
}
