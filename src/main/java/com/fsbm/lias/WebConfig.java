/*package com.fsbm.lias;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cette ligne permet au navigateur d'accéder aux fichiers dans C:/lias_uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/C:/lias_uploads/");
    }
}*/
package com.fsbm.lias;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired private ModuleAccessInterceptor moduleAccessInterceptor; // ✅ AJOUT

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/C:/lias_uploads/");
    }

    // ✅ NOUVELLE MÉTHODE QUI ACTIVE LE VERROU
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduleAccessInterceptor);
    }
}