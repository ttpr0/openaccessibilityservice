package org.tud.oas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.tud.oas.util.context.ContextInterceptor;

@Configuration
public class ContextConfig implements WebMvcConfigurer {
    public ContextInterceptor contextInterceptor() {
        return new ContextInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(contextInterceptor()).addPathPatterns("/**");
    }
}
