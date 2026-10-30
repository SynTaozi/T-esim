package com.tesim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * T-esim 后端启动类（默认 profile=dev）
 */
@SpringBootApplication
public class TEsimApplication {

    public static void main(String[] args) {
        SpringApplication.run(TEsimApplication.class, args);
    }
}
