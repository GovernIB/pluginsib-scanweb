package org.fundaciobit.pluginsib.scanweb.springboottester.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@org.springframework.context.annotation.Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

  @Bean
  public ViewResolver viewResolver() {
    final InternalResourceViewResolver bean = new InternalResourceViewResolver();
    bean.setViewClass(JstlView.class);
    bean.setPrefix("/WEB-INF/jsp/");
    bean.setSuffix(".jsp");
    return bean;
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    registry.viewResolver(viewResolver());
  }
  
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/css/**","/js/**","/img/**")
        .addResourceLocations("classpath:/static/css/","classpath:/static/js/","classpath:/static/img/")
        .setCachePeriod(3600)
        .resourceChain(true)
        .addResolver(new PathResourceResolver());
  }
  
  
}
