package ua.dp.hammer.smarthome.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.File;

public class ApplicationInitializer implements WebApplicationInitializer {

   @Override
   public void onStartup(ServletContext container) throws ServletException {
      // Sets Log4j2 config file location
      LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
      // this will force a reconfiguration
      loggerContext.setConfigLocation(new File(System.getProperty("log4j2ConfigFile")).toURI());

      // Create the 'root' Spring application context
      AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
      // Manage the lifecycle of the root application context
      container.addListener(new ContextLoaderListener(context));

      // Creates the root application context
      context.setConfigLocation("ua.dp.hammer.smarthome.config");

      ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new DispatcherServlet(context));
      dispatcher.setLoadOnStartup(1);
      dispatcher.addMapping("/");
   }
}
