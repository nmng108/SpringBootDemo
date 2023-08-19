package com.example.demo.configuration;

import com.example.demo.dao.CustomRepository;
import com.example.demo.dao.Impl.CustomRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.demo.dao", repositoryBaseClass = CustomRepositoryImpl.class)
public class RepositoryConfiguration {
}
