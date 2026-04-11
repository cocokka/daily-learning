package com.demo.dl.ocr.platform.config;

import com.demo.dl.ocr.platform.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

  @Value("${app.snowflake.worker-id:1}")
  private long workerId;

  @Value("${app.snowflake.datacenter-id:1}")
  private long datacenterId;

  @Bean
  public SnowflakeIdGenerator snowflakeIdGenerator() {
    return new SnowflakeIdGenerator(workerId, datacenterId);
  }
}
