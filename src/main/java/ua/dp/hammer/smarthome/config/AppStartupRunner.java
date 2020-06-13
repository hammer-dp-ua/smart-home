package ua.dp.hammer.smarthome.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AppStartupRunner {
   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
   }
}
