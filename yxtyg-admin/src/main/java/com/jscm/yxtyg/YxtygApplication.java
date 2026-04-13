package com.jscm.yxtyg;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 一线体验官专项数据分析系统启动类
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.jscm.yxtyg.mapper")
@EnableAsync
public class YxtygApplication {

    public static void main(String[] args) {
        SpringApplication.run(YxtygApplication.class, args);
        System.out.println("====================================");
        System.out.println("一线体验官专项数据分析系统启动成功！");
        System.out.println("====================================");
    }

}
