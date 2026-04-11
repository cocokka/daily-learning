package com.demo.dl.ocr.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ReactorConfig {

  @Bean
  public ReactorResourceFactory reactorResourceFactory() {
    return new ReactorResourceFactory();
  }

  @Bean
  public Scheduler jdbcScheduler() {
    return Schedulers.newBoundedElastic(50, 100, "jdbc-pool");
  }

  @Bean
  public Scheduler ioScheduler() {
    return Schedulers.newBoundedElastic(100, 200, "io-pool");
  }
}
