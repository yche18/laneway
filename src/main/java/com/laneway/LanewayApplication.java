package com.laneway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.laneway.mapper")
@SpringBootApplication
public class LanewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(LanewayApplication.class, args);
  }
}
