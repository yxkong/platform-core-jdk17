package com.github.platform.core.common.annotation;

import com.github.platform.core.common.configuration.condition.InListConditional;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 自定义条件主键，判断对应的havingValue 是否在列表中
 * @author: yxkong
 * @date: 2023/2/18 11:13 AM
 * @version: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(InListConditional.class)
public @interface ConditionalOnPropertyInList {

    /**
     * 属性名前缀
     */
    String prefix() default "";

    /**
     * 要检查的属性名
     */
    String[] name();

    /**
     * 需要匹配的值
     */
    String havingValue();

    /**
     * 当属性不存在时是否匹配
     */
    boolean matchIfMissing() default false;
}
