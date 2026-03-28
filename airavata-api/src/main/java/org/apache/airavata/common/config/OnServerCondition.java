package org.apache.airavata.common.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import java.util.Arrays;
import java.util.Map;

public class OnServerCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnServer.class.getName());
        if (attrs == null) return false;
        String requiredServer = (String) attrs.get("value");
        String[] servers = context.getEnvironment().getProperty("airavata.servers", String[].class);
        if (servers == null) return true; // Default: all enabled
        return Arrays.asList(servers).contains(requiredServer);
    }
}
