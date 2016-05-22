package ua.dp.hammer.smarthome.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import ua.dp.hammer.smarthome.beans.GoogleDriveUploaderBean;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackages = "ua.dp.hammer.smarthome.beans",
      excludeFilters = @ComponentScan.Filter(value = GoogleDriveUploaderBean.class, type = FilterType.ASSIGNABLE_TYPE))
@EnableAsync
@EnableScheduling
public class AppConfig {

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
   }
}