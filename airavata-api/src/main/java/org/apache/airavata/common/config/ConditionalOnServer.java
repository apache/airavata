package org.apache.airavata.common.config;

import org.springframework.context.annotation.Conditional;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnServerCondition.class)
public @interface ConditionalOnServer {
    String value();
}
