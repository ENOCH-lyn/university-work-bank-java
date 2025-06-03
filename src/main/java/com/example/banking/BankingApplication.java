package com.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BankingApplication
 *
 * Spring Boot 应用程序入口类。
 */
@SpringBootApplication
public class BankingApplication {
    /**
     * 应用程序主方法，启动Spring Boot应用。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}