package com.straviso.ns.dispatchcontrollercore.multitenancy;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceContext;
import javax.servlet.Filter;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;
import com.straviso.ns.dispatchcontrollercore.security.JwtRequestFilter;

@Configuration
@EnableConfigurationProperties(JpaProperties.class)
public class MultiTenancyJpaConfiguration {

   
     @Autowired 
     private MultiTenantConnectionProvider multiTenantConnectionProvider;
    
     @Autowired 
     AutowireCapableBeanFactory beanFactory;
     
     @Autowired
     private CurrentTenantIdentifierResolver currentTenantIdentifierResolver;
     
     @PersistenceContext 
     @Primary
     @Bean
      public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
     	
         Map<String, Object> props = new HashMap<>();
         props.put("hibernate.multiTenancy", MultiTenancyStrategy.DATABASE.name());
         props.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
         props.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);

         LocalContainerEntityManagerFactoryBean result = builder.dataSource(dataSource())
                 .persistenceUnit(MultiTenantConstants.TENANT_KEY)
                 .properties(props)
                 .packages("com").build();
         result.setJpaVendorAdapter(jpaVendorAdapter());
         return result;
     }
     
     @Bean
     @Primary
     @ConfigurationProperties(prefix = "spring.datasource")
     public DataSource dataSource() {
         return DataSourceBuilder.create().build();
     }
     
     @Bean
     public JpaVendorAdapter jpaVendorAdapter() {
     	
         HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
         hibernateJpaVendorAdapter.setShowSql(false);
         hibernateJpaVendorAdapter.setGenerateDdl(false);
         return hibernateJpaVendorAdapter;
     }

     @SuppressWarnings({ "rawtypes", "unchecked" })
 	@Bean
     public FilterRegistrationBean myFilter() {
     	
         FilterRegistrationBean registration = new FilterRegistrationBean();
         Filter tenantFilter = new JwtRequestFilter();
         beanFactory.autowireBean(tenantFilter);
         registration.setFilter(tenantFilter);
         registration.addUrlPatterns("/*");
         return registration;
     }
     
}
