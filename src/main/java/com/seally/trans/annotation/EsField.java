package com.seally.trans.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface EsField {
	String value() default "";
	String type() default "String"; //特殊类型字段标注
	String format() default "yyyy-MM-dd HH:mm:ss"; //用于处理特殊字段日期格式时所用格式
}
