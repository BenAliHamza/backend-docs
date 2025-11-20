package tn.esprit.docsbackend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS configuration allowing cross-origin requests from any origin.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        // Configure CORS
        CorsConfiguration config = new CorsConfiguration();
        // Allow cookies / Authorization header if needed
        config.setAllowCredentials(true);
        // Allow any origin (using patterns to work with allowCredentials)
        config.addAllowedOriginPattern("*");
        // Allow all headers (Authorization, Content-Type, etc.)
        config.addAllowedHeader("*");
        // Allow all HTTP methods
        config.addAllowedMethod("*");

        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // Register the filter
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0); // Ensure this runs early
        return bean;
    }
}
