package com.fleety.olympics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Value("${api.version}")
    public String apiVersion;
}