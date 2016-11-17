package ua.dp.hammer.smarthome.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
   public static final String ERROR_OCCURRED_ATTRIBUTE = "ERROR_OCCURRED";

   private Logger logger;

   @Override
   public void onStartup(ServletContext container) throws ServletException {
      // Sets Log4j2 config file location
      LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
      // this will force a reconfiguration
      loggerContext.setConfigLocation(new File(System.getProperty("log4j2ConfigFile")).toURI());
      logger = LogManager.getLogger(ApplicationInitializer.class);

      super.onStartup(container);
   }

   @Override
   protected Class<?>[] getRootConfigClasses() {
      return new Class[] {AppConfig.class, WebConfig.class};
   }

   @Override
   protected Class<?>[] getServletConfigClasses() {
      return new Class<?>[0];
   }

   @Override
   protected String[] getServletMappings() {
      return new String[] {"/"};
   }

   @Override
   protected Filter[] getServletFilters() {
      AbstractRequestLoggingFilter requestLoggingFilter = new AbstractRequestLoggingFilter() {

         @Override
         protected void beforeRequest(HttpServletRequest request, String message) {
         }

         @Override
         protected void afterRequest(HttpServletRequest request, String message) {
            boolean isErrorOccurred = request.getAttribute(ERROR_OCCURRED_ATTRIBUTE) != null ?
                  (Boolean) request.getAttribute(ERROR_OCCURRED_ATTRIBUTE) : false;

            if (isErrorOccurred) {
               logger.error(message);
            }
         }
      };

      requestLoggingFilter.setIncludePayload(true);
      requestLoggingFilter.setMaxPayloadLength(500);
      requestLoggingFilter.setIncludeQueryString(false);
      requestLoggingFilter.setBeforeMessagePrefix("");
      requestLoggingFilter.setAfterMessagePrefix("");
      requestLoggingFilter.setBeforeMessageSuffix("");
      requestLoggingFilter.setAfterMessageSuffix("");
      return new Filter[]{requestLoggingFilter};
   }
}
