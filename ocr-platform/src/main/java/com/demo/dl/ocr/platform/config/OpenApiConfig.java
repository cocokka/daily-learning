package com.demo.dl.ocr.platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "OCR Platform API", version = "1.0", description = "OCR识别平台API文档"),
    servers = {@Server(url = "/", description = "Default Server URL")})
public class OpenApiConfig {}
