package com.example.be_voluongquang.configuation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/view/");
        bean.setSuffix(".jsp");
        return bean;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(viewResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/resources/css/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/**");

        registry.addResourceHandler("/images/product/cool_prd/**")
                .addResourceLocations("file:uploads/images/product/cool_prd/");

                registry.addResourceHandler("/images/product/snack/**")
                .addResourceLocations("file:uploads/images/product/snack/");

                registry.addResourceHandler("/images/product/dry_prd/**")
                .addResourceLocations("file:uploads/images/product/dry_prd/");

                registry.addResourceHandler("/images/product/spice/**")
                .addResourceLocations("file:uploads/images/product/spice/");

                registry.addResourceHandler("/images/product/other/**")
                .addResourceLocations("file:uploads/images/product/other/");

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
