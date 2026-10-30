package com.tesim.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.tesim.mapper")
public class MybatisPlusConfig {
}
