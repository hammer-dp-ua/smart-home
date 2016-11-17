package ua.dp.hammer.smarthome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan("ua.dp.hammer.smarthome.controllers")
public class WebConfig extends WebMvcConfigurerAdapter {

   @Override
   public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/").setViewName("index");
   }

   @Override
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/classic/**").addResourceLocations("/WEB-INF/classic/");
      registry.addResourceHandler("/ext/**").addResourceLocations("/WEB-INF/ext/");
      registry.addResourceHandler("/classic.json").addResourceLocations("/WEB-INF/classic.json");
      registry.addResourceHandler("/cache.appcache").addResourceLocations("/WEB-INF/cache.appcache");
   }

   @Override
   public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
      configurer.defaultContentType(MediaType.APPLICATION_JSON);
      configurer.ignoreAcceptHeader(true);
   }

   /*@Override
   public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
      HttpMessageConverter converter = new StringHttpMessageConverter();
      converters.add(converter);

      converter = new MappingJackson2HttpMessageConverter();
      converters.add(converter);
   }*/

   @Bean
   public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
      ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
      resolver.setContentNegotiationManager(manager);
      return resolver;
   }

   @Bean
   public ViewResolver getJspViewResolver() {
      InternalResourceViewResolver resolver = new InternalResourceViewResolver();
      resolver.setPrefix("WEB-INF/jsp/");
      resolver.setSuffix(".jsp");
      resolver.setViewClass(JstlView.class);
      return resolver;
   }
}
