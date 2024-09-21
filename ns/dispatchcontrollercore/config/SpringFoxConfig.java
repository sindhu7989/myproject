package com.straviso.ns.dispatchcontrollercore.config;





import java.util.Arrays;
import java.util.Collections;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;

import springfox.documentation.service.SecurityReference;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {    
    @Bean
    public Docket api() {  	
     	 return new Docket(DocumentationType.SWAGGER_2)
     		      .apiInfo(apiInfo())
     		      .securityContexts(Arrays.asList(securityContext()))
     		      .securitySchemes(Arrays.asList(apiKey()))
     		      .select()
     		      .apis(RequestHandlerSelectors.basePackage("com.straviso.ns.dispatchcontrollercore"))
     		      .paths(PathSelectors.any())
     		      .build();
   
    }
	 
    private ApiInfo apiInfo() {
        return new ApiInfo(
          "DISPATCH CONTROLLER", 
          "Technician and Tickets details API's", 
          "API TOS", 
          "Terms of service", 
          new Contact("Straviso", "www.straviso.com", "myeaddress@straviso.com"), 
          "License of API", "API license URL", Collections.emptyList());
    }
    
    private ApiKey apiKey() { 
        return new ApiKey("JWT", "Authorization", "header"); 
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
          .securityReferences(Arrays.asList(defaultAuth()))
          .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
          .build();
    }

    private SecurityReference defaultAuth() {
        return SecurityReference.builder()
          .scopes(new AuthorizationScope[0])
          .reference("JWT")
          .build();
    }
    
   
}