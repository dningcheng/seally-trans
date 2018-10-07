package com.seally.trans;

import java.util.Arrays;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Date 2017年11月20日
 * @author dnc
 * 
 */
@SpringBootApplication
@EnableTransactionManagement  //开启注解事务管理，等同于xml配置文件中的 <tx:annotation-driven />
@MapperScan("com.seally.trans.mapper") //mybatis的映射文件包路径
public class Application {

	public static void main(String[] args) {
		System.out.println("传递参数："+Arrays.toString(args));
		SpringApplication.run(Application.class, args);
	}
	
}
