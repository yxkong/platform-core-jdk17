package com.github.platform.core.common.configuration.condition;

import com.github.platform.core.common.annotation.ConditionalOnPropertyInList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: yxkong
 * @date: 2023/2/18 11:17 AM
 * @version: 1.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InListConditional implements Condition{
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnPropertyInList.class.getName());
        // 获取name数组的第一个值
        String propertyName = ((String[])attrs.get("name"))[0];
        String havingValue = (String) attrs.get("havingValue");

        // 获取属性值（支持逗号分隔列表和YAML数组两种格式）
        String propertyValue = context.getEnvironment().getProperty(propertyName);
        if (propertyValue == null) {
            return false;
        }

        // 处理两种格式：
        // 1. 逗号分隔：file.routers=aliyun,ucloud
        // 2. YAML数组：file.routers: [aliyun, ucloud]
        List<String> values = Arrays.asList(propertyValue.split("\\s*,\\s*"));
        return values.contains(havingValue);
    }
}
