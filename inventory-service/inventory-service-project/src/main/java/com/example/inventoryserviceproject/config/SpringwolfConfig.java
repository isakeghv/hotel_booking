package com.example.inventoryserviceproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the Springwolf UI (Kafka documentation) under /inventory/springwolf,
 * so it is reachable through the gateway, which only routes /inventory/** to
 * this service.
 *
 * <p>The UI files are always packaged at /springwolf, so they are mapped here.
 * The documentation itself (/inventory/springwolf/docs) is moved with
 * {@code springwolf.path.base} in application.properties. The UI finds it
 * relative to its own address.
 */
@Configuration
public class SpringwolfConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/inventory/springwolf/**")
                .addResourceLocations("classpath:/META-INF/resources/springwolf/");
    }
}
