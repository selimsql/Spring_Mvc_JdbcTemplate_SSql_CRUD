package com.selimsql.lesson.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan({ "com.selimsql.lesson.configuration" })
@PropertySource(value = { "classpath:spring/application.properties" })
public class DbConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public DataSource getDataSource()
    {
       BasicDataSource dataSource = new BasicDataSource();

       String driverClassName = environment.getProperty("jdbc.driverClassName");
       dataSource.setDriverClassName(driverClassName);

       String driverUrl = environment.getProperty("jdbc.url");
       dataSource.setUrl(driverUrl);

       dataSource.setUsername(environment.getProperty("jdbc.username"));
       dataSource.setPassword(environment.getProperty("jdbc.password"));

       return dataSource;
   }
}
