package com.seally.trans.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface EsDocument {
	String type(); //插入es的类型
	String index();
	//String idMethodName() default "getId"; //获取主键的方法名
}
