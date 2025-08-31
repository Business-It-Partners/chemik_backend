package com.chemiki.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://192.168.18.12:8080","http://192.168.18.12:5173") // Add mobile app origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve post images
        registry.addResourceHandler("/uploads/post-images/**")
                .addResourceLocations("file:uploads/post-images/");

        // Serve marketplace images
        registry.addResourceHandler("/uploads/marketplace-images/**")
                .addResourceLocations("file:uploads/marketplace-images/");
        // 🔥 NEW: Serve profile images
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:uploads/profile-images/");

        // Serve any other uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

}