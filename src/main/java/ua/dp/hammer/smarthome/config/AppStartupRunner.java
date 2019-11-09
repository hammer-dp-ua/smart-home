package ua.dp.hammer.smarthome.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class AppStartupRunner {
   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      // Sets Log4j2 config file location
      LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);

      File log4j2ConfigFile = new File(environment.getRequiredProperty("log4j2ConfigFile"));

      if (!log4j2ConfigFile.exists()) {
         throw new IllegalStateException(log4j2ConfigFile + " file doesn't exist");
      }

      // this will force a reconfiguration
      loggerContext.setConfigLocation(log4j2ConfigFile.toURI());
      loggerContext.reconfigure();
   }
}
