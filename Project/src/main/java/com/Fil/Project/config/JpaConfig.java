package com.Fil.Project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Configuration
@EnableJpaRepositories(basePackages = "com.Fil.Project.repository")
@EntityScan(basePackages = "com.Fil.Project.model")
public class JpaConfig {
}